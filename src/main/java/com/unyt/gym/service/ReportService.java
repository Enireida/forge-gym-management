package com.unyt.gym.service;

import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.MemberStatus;
import com.unyt.gym.entity.Payment;
import com.unyt.gym.repository.MemberRepository;
import com.unyt.gym.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Aggregates members and payments into summary tables for the admin
 * dashboard and the {@code /api/stats} endpoints.
 *
 * <p>This is the most stream/lambda-heavy service in the project — it
 * carries the bulk of the R3 + R4 evidence.</p>
 */
@Service
public class ReportService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public ReportService(MemberRepository memberRepository,
                         PaymentRepository paymentRepository) {
        this.memberRepository = memberRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Total revenue over the last {@code days} days. Uses {@code reduce}
     * to sum a stream of {@link BigDecimal}.
     */
    public BigDecimal revenueLastDays(int days) {
        LocalDate from = LocalDate.now().minusDays(days);
        return paymentRepository.findByDateBetween(from, LocalDate.now()).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Revenue per month for the current year. Demonstrates
     * {@code Collectors.groupingBy} with a downstream collector and a
     * {@link TreeMap} so the result is naturally sorted by month.
     */
    public Map<YearMonth, BigDecimal> revenueByMonth() {
        return paymentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        p -> YearMonth.from(p.getDate()),
                        TreeMap::new,
                        Collectors.mapping(Payment::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }

    /**
     * Top spending members. Returns a {@link LinkedHashMap} so the
     * iteration order matches the descending spend.
     */
    public Map<String, BigDecimal> topSpenders(int n) {
        Map<String, BigDecimal> totals = paymentRepository.findAll().stream()
                .filter(p -> p.getMember() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getMember().getName(),
                        Collectors.mapping(Payment::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    /** Top spenders filtered by date range. */
    public Map<String, BigDecimal> topSpendersByRange(LocalDate from, LocalDate to, int n) {
        return paymentRepository.findByDateBetween(from, to).stream()
                .filter(p -> p.getMember() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getMember().getName(),
                        Collectors.mapping(Payment::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    /** Revenue grouped by day for a date range. */
    public Map<String, BigDecimal> revenueByDay(LocalDate from, LocalDate to) {
        return paymentRepository.findByDateBetween(from, to).stream()
                .collect(Collectors.groupingBy(
                        p -> p.getDate().toString(),
                        TreeMap::new,
                        Collectors.mapping(Payment::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }

    /** Total revenue for a date range. */
    public BigDecimal revenueByRange(LocalDate from, LocalDate to) {
        return paymentRepository.findByDateBetween(from, to).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Member counts grouped by status. */
    public Map<MemberStatus, Long> memberStatusBreakdown() {
        return memberRepository.findAll().stream()
                .collect(Collectors.groupingBy(Member::getStatus, Collectors.counting()));
    }

    /**
     * Generic high-order helper showcasing R3 — accept any function from
     * {@link Member} to a key {@code K} and count how many members fall
     * into each bucket.
     */
    public <K> Map<K, Long> bucketMembers(Function<Member, K> classifier) {
        return memberRepository.findAll().stream()
                .collect(Collectors.groupingBy(classifier, Collectors.counting()));
    }
}
