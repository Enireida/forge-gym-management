package com.unyt.gym.service;

import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.MemberStatus;
import com.unyt.gym.exception.DuplicateResourceException;
import com.unyt.gym.exception.MemberNotFoundException;
import com.unyt.gym.pattern.EventBus;
import com.unyt.gym.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Business operations on {@link Member}.
 *
 * <p>Touches several requirements at once:</p>
 * <ul>
 *   <li><b>R1 Collections</b> - uses {@link ArrayList}, {@link HashMap},
 *       and {@link TreeSet} below.</li>
 *   <li><b>R3 Lambdas</b> - predicates and comparators expressed as lambdas.</li>
 *   <li><b>R4 Streams</b> - filter / map / sorted / collect.</li>
 *   <li><b>R7 JPA</b> - uses Spring Data through {@link MemberRepository}.</li>
 *   <li><b>R10 Exceptions</b> - throws {@link MemberNotFoundException}
 *       and {@link DuplicateResourceException}.</li>
 * </ul>
 */
@Service
public class MemberService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final MemberRepository memberRepository;
    private final EmailService emailService;

    public MemberService(MemberRepository memberRepository, EmailService emailService) {
        this.memberRepository = memberRepository;
        this.emailService = emailService;
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    public Optional<Member> findByPin(String pin) {
        return memberRepository.findByPin(pin);
    }

    @Transactional
    public Member create(Member member) {
        if (member.getEmail() != null
                && memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Member email", member.getEmail());
        }
        member.setPin(generateUniquePin());
        if (member.getStartDate() == null) member.setStartDate(LocalDate.now());
        if (member.getStatus() == null) member.setStatus(MemberStatus.ACTIVE);
        Member saved = memberRepository.save(member);

        EventBus.getInstance().publish("member.created", saved);
        if (saved.getStatus() == MemberStatus.PENDING) {
            emailService.sendRegistrationPending(saved);
        } else {
            emailService.sendMembershipCreated(saved);
        }
        return saved;
    }

    @Transactional
    public Member update(Long id, Member patch) {
        Member existing = findById(id);

        boolean membershipChanged =
            (patch.getPlan()       != null && !patch.getPlan().equals(existing.getPlan()))       ||
            (patch.getExpiryDate() != null && !patch.getExpiryDate().equals(existing.getExpiryDate())) ||
            (patch.getStatus()     != null && !patch.getStatus().equals(existing.getStatus()));

        if (patch.getName() != null)      existing.setName(patch.getName());
        if (patch.getPhone() != null)     existing.setPhone(patch.getPhone());
        if (patch.getStatus() != null)    existing.setStatus(patch.getStatus());
        if (patch.getPlan() != null)      existing.setPlan(patch.getPlan());
        if (patch.getExpiryDate() != null) existing.setExpiryDate(patch.getExpiryDate());
        if (patch.getWeightKg() != null)  existing.setWeightKg(patch.getWeightKg());
        if (patch.getHeightCm() != null)  existing.setHeightCm(patch.getHeightCm());
        // rfidCode allows null (unassigning a card) so we always copy it.
        existing.setRfidCode(patch.getRfidCode());
        Member saved = memberRepository.save(existing);

        if (membershipChanged) {
            emailService.sendMembershipUpdated(saved);
        }
        return saved;
    }

    @Transactional
    public void activate(Long id) {
        Member m = findById(id);
        m.setStatus(MemberStatus.ACTIVE);
        if (m.getStartDate() == null) m.setStartDate(LocalDate.now());
        if (m.getExpiryDate() == null && m.getPlan() != null) {
            m.setExpiryDate(LocalDate.now().plusMonths(m.getPlan().getDurationMonths()));
        }
        memberRepository.save(m);
        emailService.sendMembershipCreated(m);
    }

    @Transactional
    public void delete(Long id) {
        Member m = findById(id);
        emailService.sendMembershipDeleted(m);
        memberRepository.delete(m);
    }

    /* ============================================================
       Stream / Lambda heavy operations (R3, R4)
       ============================================================ */

    /**
     * Searches active members by name or email using a database-side JPQL
     * query instead of loading all members into memory.
     *
     * <p>Previously used {@code findAll()} + Java stream filter which read
     * every member row into the JVM. The repository query now does the
     * filtering and ordering in SQL — significantly more efficient at scale.</p>
     */
    public List<Member> searchActive(String query) {
        return memberRepository.searchActiveByNameOrEmail(
                query == null || query.isBlank() ? null : query.trim());
    }

    public Map<String, List<Member>> groupByPlan() {
        return memberRepository.findAll().stream()
                .filter(m -> m.getPlan() != null)
                .collect(Collectors.groupingBy(
                        m -> m.getPlan().getName(),
                        HashMap::new,
                        Collectors.toList()));
    }

    public List<Member> findExpiringSoon(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return memberRepository.findExpiringBefore(cutoff);
    }

    public Map<MemberStatus, Long> statusCounts() {
        return memberRepository.findAll().stream()
                .collect(Collectors.groupingBy(Member::getStatus, Collectors.counting()));
    }

    public Set<String> distinctPlans() {
        return memberRepository.findAll().stream()
                .map(m -> m.getPlan() == null ? "(none)" : m.getPlan().getName())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /* ============================================================
       Helpers
       ============================================================ */

    private String generateUniquePin() {
        for (int attempt = 0; attempt < 1000; attempt++) {
            String candidate = String.format("%04d", RANDOM.nextInt(10000));
            if (!memberRepository.existsByPin(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to allocate a unique PIN after 1000 attempts");
    }
}
