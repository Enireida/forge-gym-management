package com.unyt.gym.service;

import com.unyt.gym.entity.Member;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * In-memory ring of recent member check-ins (PIN or RFID at the kiosk).
 * The admin's browser polls {@code /admin/api/checkin-events?since=<ts>}
 * every few seconds; we return any events newer than that timestamp so the
 * admin can show a 3-second toast in the corner.
 *
 * <p>Bounded to {@link #CAPACITY} so memory is fixed. Events live until the
 * JVM restarts (acceptable — toasts only matter live).</p>
 */
@Service
public class CheckInEventService {

    private static final int CAPACITY = 50;

    /** Newest events go to the FRONT; oldest get evicted from the back. */
    private final Deque<CheckInEvent> events = new ConcurrentLinkedDeque<>();

    public void recordCheckIn(Member member) {
        if (member == null) return;
        CheckInEvent ev = new CheckInEvent();
        ev.id          = System.currentTimeMillis();
        ev.timestamp   = ev.id;
        ev.memberId    = member.getId();
        ev.memberName  = member.getName();
        ev.planName    = member.getPlan() != null ? member.getPlan().getName() : "—";
        ev.expiryDate  = member.getExpiryDate();
        ev.photoData   = member.getPhotoData();
        events.addFirst(ev);
        while (events.size() > CAPACITY) events.removeLast();
    }

    /** Returns the events with {@code timestamp > since}, newest first. */
    public List<CheckInEvent> eventsSince(long since) {
        List<CheckInEvent> out = new ArrayList<>();
        for (CheckInEvent e : events) {
            if (e.timestamp > since) out.add(e);
        }
        return out;
    }

    /** Plain holder — public fields so Jackson serialises without extra annotations. */
    public static class CheckInEvent {
        public long       id;
        public long       timestamp;
        public Long       memberId;
        public String     memberName;
        public String     planName;
        public LocalDate  expiryDate;
        public String     photoData;
    }
}
