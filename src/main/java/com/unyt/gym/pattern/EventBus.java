package com.unyt.gym.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * <b>Singleton + Observer Pattern</b> (GoF) — a process-wide pub/sub bus.
 *
 * <p>Components register listeners via {@link #subscribe(String, Consumer)}
 * and emit events via {@link #publish(String, Object)}. The class itself
 * is a thread-safe lazy-initialised singleton (Bill Pugh idiom).</p>
 *
 * <p>Two design patterns covered in one class — that is intentional and
 * explicitly justified in the report.</p>
 *
 * <p><b>Course requirement:</b> R9 Design Patterns.
 * R5 Concurrency: uses {@link ConcurrentHashMap} and
 * {@link CopyOnWriteArrayList} to be thread-safe.</p>
 */
public final class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    private final Map<String, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    /** Bill Pugh holder idiom — thread-safe lazy singleton. */
    private static final class Holder {
        private static final EventBus INSTANCE = new EventBus();
    }

    private EventBus() { }

    public static EventBus getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Register a listener for a topic.
     *
     * @param topic topic name, e.g. {@code "member.created"}
     * @param handler callback, executed synchronously on publish
     */
    @SuppressWarnings("unchecked")
    public <T> void subscribe(String topic, Consumer<T> handler) {
        listeners.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>())
                 .add((Consumer<Object>) handler);
        log.debug("Subscribed handler for '{}'", topic);
    }

    /**
     * Publish a payload. Each subscriber's exception is caught so that one
     * failing listener cannot break the publishing thread.
     */
    public void publish(String topic, Object payload) {
        List<Consumer<Object>> handlers = listeners.get(topic);
        if (handlers == null) return;
        for (Consumer<Object> h : handlers) {
            try {
                h.accept(payload);
            } catch (RuntimeException ex) {
                // R10 — never an empty catch block. Log and continue.
                log.warn("Listener for topic '{}' threw {}", topic, ex.getMessage());
            }
        }
    }
}
