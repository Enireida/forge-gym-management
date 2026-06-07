package com.unyt.gym.service;

import com.unyt.gym.entity.Attendance;
import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.MemberStatus;
import com.unyt.gym.exception.GymException;
import com.unyt.gym.exception.MemberNotFoundException;
import com.unyt.gym.pattern.EventBus;
import com.unyt.gym.repository.AttendanceRepository;
import com.unyt.gym.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Handles PIN-based and RFID-based check-in/check-out at the door kiosk.
 *
 * <p>The check-in flow is the most concurrency-sensitive part of the
 * application: under load, two RFID taps for the same card can arrive on
 * two different request threads almost simultaneously. Without
 * synchronisation, both threads would read "no open row" and both insert
 * a new check-in row, producing duplicates.</p>
 *
 * <p>The solution applied here — and documented in the report
 * (Section 4.1 — Challenges) — is two-fold:</p>
 * <ul>
 *   <li>The toggle method is {@code @Transactional} with
 *       {@code READ_COMMITTED} isolation, so the whole
 *       find / insert / update cycle is one atomic unit of work.</li>
 *   <li>The lookup for an open attendance row uses a
 *       {@code PESSIMISTIC_WRITE} lock — see
 *       {@link AttendanceRepository#lockOpenRow(Long, LocalDate)}.
 *       The first thread acquires the row lock; the second thread blocks
 *       at the database, then re-reads up-to-date state and either
 *       closes the existing open row or proceeds to insert.</li>
 * </ul>
 *
 * <p><b>Course requirement:</b> R5 Concurrency / Multithreading;
 * R9 publishes {@code "attendance.checkin"} / {@code "attendance.checkout"}
 * events through the Observer-pattern {@link EventBus}.</p>
 */
@Service
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);

    private final MemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;
    private final CheckInEventService checkInEventService;

    public AttendanceService(MemberRepository memberRepository,
                             AttendanceRepository attendanceRepository,
                             CheckInEventService checkInEventService) {
        this.memberRepository    = memberRepository;
        this.attendanceRepository = attendanceRepository;
        this.checkInEventService = checkInEventService;
    }

    /** Toggle check-in state by 4-digit PIN. */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Attendance checkInOrOutByPin(String pin) {
        Member member = memberRepository.findByPin(pin)
                .orElseThrow(() -> new MemberNotFoundException("PIN " + pin));
        Attendance a = toggle(member);
        recordToastIfCheckIn(member, a);
        return a;
    }

    /** Toggle check-in state by 10-digit RFID card code. */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Attendance checkInOrOutByRfid(String rfidCode) {
        Member member = memberRepository.findByRfidCode(rfidCode)
                .orElseThrow(() -> new MemberNotFoundException("RFID " + rfidCode));
        // Plan-tier gating: RFID is a perk of Standard and above.
        if (member.getPlan() == null || !member.getPlan().isAllowRfid()) {
            throw new GymException(
                    "RFID access is not included in this plan. Please use your 4-digit PIN, or upgrade.",
                    403);
        }
        Attendance a = toggle(member);
        recordToastIfCheckIn(member, a);
        return a;
    }

    /** Push to the live admin-toast feed only when this toggle was a fresh check-IN. */
    private void recordToastIfCheckIn(Member member, Attendance a) {
        if (a != null && a.getCheckOut() == null) {
            checkInEventService.recordCheckIn(member);
        }
    }

    /**
     * The actual open / close logic, protected by a pessimistic row lock.
     *
     * <p>Two threads that hit this method for the same member at the
     * same time will serialise on {@code lockOpenRow}: the second one
     * waits at the database row-lock, then sees the now-closed row and
     * proceeds to insert a fresh check-in. No duplicates are possible.</p>
     */
    private Attendance toggle(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new GymException("Membership is not active. Please contact reception.", 403);
        }
        LocalDate today = LocalDate.now();

        Optional<Attendance> locked = attendanceRepository.lockOpenRow(member.getId(), today);

        if (locked.isPresent()) {
            // CHECK-OUT path: close the open session and publish event.
            Attendance row = locked.get();
            row.setCheckOut(LocalTime.now().withNano(0));
            Attendance saved = attendanceRepository.save(row);
            log.info("check-out  member={} (id={}) at={}",
                    member.getName(), member.getId(), saved.getCheckOut());
            EventBus.getInstance().publish("attendance.checkout", saved);
            return saved;
        }

        // CHECK-IN path: insert a brand new attendance row.
        Attendance fresh = new Attendance(member);
        fresh.setCheckIn(LocalTime.now().withNano(0));
        Attendance saved = attendanceRepository.save(fresh);
        log.info("check-in   member={} (id={}) at={}",
                member.getName(), member.getId(), saved.getCheckIn());
        EventBus.getInstance().publish("attendance.checkin", saved);
        return saved;
    }

    public List<Attendance> getToday() {
        return attendanceRepository.findByDate(LocalDate.now());
    }

    public List<Attendance> getRange(LocalDate from, LocalDate to) {
        return attendanceRepository.findByDateBetween(from, to);
    }
}
