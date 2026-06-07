package com.unyt.gym.service;

import com.unyt.gym.entity.Member;
import com.unyt.gym.pattern.NotificationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrency hub for the gym application.
 *
 * <p>Three different facets of the {@code java.util.concurrent} stack are
 * deliberately exercised here so the live demo can show all of them:</p>
 * <ul>
 *   <li>An {@link ExecutorService} fixed thread pool for batch jobs.</li>
 *   <li>{@link CompletableFuture} for non-blocking aggregate computation.</li>
 *   <li>{@code @Async} + {@code @Scheduled} for daily expiry reminders.</li>
 *   <li>An {@link AtomicInteger} as a thread-safe counter.</li>
 * </ul>
 *
 * <p><b>Course requirement:</b> R5 Concurrency / Multithreading.</p>
 */
@Service
public class ConcurrencyService {

    private static final Logger log = LoggerFactory.getLogger(ConcurrencyService.class);

    @Value("${gym.executor.pool-size:4}")
    private int poolSize;

    private ExecutorService executor;

    /** Thread-safe counter of background jobs that have completed. */
    private final AtomicInteger jobsCompleted = new AtomicInteger();

    private final MemberService memberService;

    public ConcurrencyService(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostConstruct
    void init() {
        executor = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r, "gym-bg-worker");
            t.setDaemon(true);
            return t;
        });
        log.info("ConcurrencyService initialised with pool size {}", poolSize);
    }

    @PreDestroy
    void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * Submit an arbitrary task to the gym's background worker pool.
     *
     * @return Future that completes when the task finishes
     */
    public Future<?> submit(Runnable task) {
        return executor.submit(() -> {
            try {
                task.run();
            } finally {
                jobsCompleted.incrementAndGet();
            }
        });
    }

    /**
     * Generate a renewal-revenue forecast in the background using
     * {@link CompletableFuture}.
     *
     * <p>Two CPU-bound stages are run on the pool and combined when both
     * complete — a classic CompletableFuture pipeline.</p>
     */
    public CompletableFuture<BigDecimal> forecastRenewalRevenueAsync(int days) {
        CompletableFuture<List<Member>> expiringFuture = CompletableFuture
                .supplyAsync(() -> memberService.findExpiringSoon(days), executor);

        return expiringFuture.thenApplyAsync(members -> members.stream()
                .filter(m -> m.getPlan() != null)
                .map(m -> m.getPlan().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add), executor);
    }

    /**
     * Daily 09:00 cron: e-mail every member whose plan expires within
     * three days. Runs on the Spring scheduler thread pool.
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Async
    public void dailyExpiryReminder() {
        List<Member> expiring = memberService.findExpiringSoon(3);
        var notifier = NotificationFactory.forChannel(NotificationFactory.Channel.EMAIL);
        expiring.forEach(m -> notifier.send(
                m.getEmail(),
                "Your gym membership is about to expire",
                "Hi " + m.getName() + ", your plan expires on " + m.getExpiryDate()));
        log.info("Sent {} renewal reminders", expiring.size());
    }

    public int getJobsCompleted() {
        return jobsCompleted.get();
    }
}
