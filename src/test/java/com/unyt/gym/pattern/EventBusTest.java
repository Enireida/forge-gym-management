package com.unyt.gym.pattern;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link EventBus}.
 *
 * <p>Exercises:</p>
 * <ul>
 *   <li>R9 — Singleton (always same instance) and Observer
 *       (subscribe / publish round-trip).</li>
 *   <li>R5 — Thread safety: concurrent publishes from many threads
 *       deliver every event to every subscriber without loss or
 *       corruption.</li>
 *   <li>R10 — a throwing listener does NOT break the publish loop for
 *       the remaining subscribers (no empty catch).</li>
 * </ul>
 */
class EventBusTest {

    @Test
    void singleton_alwaysReturnsSameInstance() {
        assertSame(EventBus.getInstance(), EventBus.getInstance());
    }

    @Test
    void publish_deliversPayloadToSubscriber() {
        AtomicReference<String> received = new AtomicReference<>();
        EventBus.getInstance().subscribe("test.topic.A", (String s) -> received.set(s));

        EventBus.getInstance().publish("test.topic.A", "hello");
        assertEquals("hello", received.get());
    }

    @Test
    void publish_toTopicWithNoSubscribers_isNoOp() {
        // Should not throw.
        EventBus.getInstance().publish("nobody.is.listening", new Object());
    }

    @Test
    void publish_oneFailingListenerDoesNotBreakOthers() {
        AtomicInteger goodCalls = new AtomicInteger();

        EventBus.getInstance().subscribe("test.topic.B", (Object o) -> {
            throw new RuntimeException("boom");
        });
        EventBus.getInstance().subscribe("test.topic.B", (Object o) -> goodCalls.incrementAndGet());

        EventBus.getInstance().publish("test.topic.B", "ping");
        assertEquals(1, goodCalls.get(),
                "the second listener must still receive the payload despite the first one throwing");
    }

    @Test
    void publish_isThreadSafeUnderHighContention() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        EventBus.getInstance().subscribe("test.topic.C", (Object o) -> counter.incrementAndGet());

        int threads = 8;
        int eventsPerThread = 250;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                try {
                    start.await();
                    for (int i = 0; i < eventsPerThread; i++) {
                        EventBus.getInstance().publish("test.topic.C", i);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS), "all publisher threads finished in time");
        assertEquals(threads * eventsPerThread, counter.get(),
                "every published event reaches the listener exactly once");
    }
}
