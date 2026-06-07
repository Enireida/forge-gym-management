package com.unyt.gym.service;

import com.unyt.gym.entity.User;
import com.unyt.gym.entity.WorkoutLog;
import com.unyt.gym.entity.Member;
import com.unyt.gym.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Handles Strava OAuth2 authentication and activity data retrieval.
 *
 * <p>Flow:
 * <ol>
 *   <li>User clicks "Connect Strava" → redirected to {@link #getAuthorizationUrl()}</li>
 *   <li>User approves → Strava calls our callback with a code</li>
 *   <li>We exchange the code for tokens via {@link #exchangeCode(String)}</li>
 *   <li>Tokens are stored on the {@link User} entity</li>
 *   <li>User clicks "Sync" → {@link #fetchActivities(String, int)} fetches real workouts</li>
 * </ol>
 */
@Service
public class StravaService {

    private static final Logger log = LoggerFactory.getLogger(StravaService.class);

    private static final String AUTH_URL    = "https://www.strava.com/oauth/authorize";
    private static final String TOKEN_URL   = "https://www.strava.com/oauth/token";
    private static final String ACTIVITIES_URL = "https://www.strava.com/api/v3/athlete/activities";

    @Value("${strava.client-id}")
    private String clientId;

    @Value("${strava.client-secret}")
    private String clientSecret;

    @Value("${strava.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepo;
    private final RestTemplate rest = new RestTemplate();

    public StravaService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /** Builds the Strava OAuth2 authorization URL. */
    public String getAuthorizationUrl() {
        return AUTH_URL
            + "?client_id=" + clientId
            + "&redirect_uri=" + redirectUri
            + "&response_type=code"
            + "&approval_prompt=auto"
            + "&scope=activity:read_all";
    }

    /**
     * Exchanges an authorization code for access + refresh tokens.
     * Saves the tokens to the User entity.
     *
     * @return the athlete's Strava ID, or null on failure
     */
    @SuppressWarnings("unchecked")
    public Long exchangeCode(String code, User user) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("client_id",     clientId);
            body.put("client_secret", clientSecret);
            body.put("code",          code);
            body.put("grant_type",    "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<Map> resp = rest.postForEntity(TOKEN_URL,
                    new HttpEntity<>(body, headers), Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<String, Object> data = resp.getBody();
                user.setStravaAccessToken((String) data.get("access_token"));
                user.setStravaRefreshToken((String) data.get("refresh_token"));
                Object exp = data.get("expires_at");
                if (exp instanceof Number) user.setStravaTokenExpiry(((Number) exp).longValue());

                Map<String, Object> athlete = (Map<String, Object>) data.get("athlete");
                if (athlete != null && athlete.get("id") instanceof Number) {
                    Long athleteId = ((Number) athlete.get("id")).longValue();
                    user.setStravaAthleteId(athleteId);
                    userRepo.save(user);
                    log.info("[STRAVA] Token exchanged for athlete {}", athleteId);
                    return athleteId;
                }
                userRepo.save(user);
            }
        } catch (Exception e) {
            log.error("[STRAVA] Token exchange failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Refreshes the access token if it is expired or about to expire.
     * Updates and saves the user if a refresh was needed.
     */
    @SuppressWarnings("unchecked")
    public void refreshIfNeeded(User user) {
        if (user.getStravaTokenExpiry() == null) return;
        long nowPlusPadding = Instant.now().getEpochSecond() + 300; // 5-min buffer
        if (user.getStravaTokenExpiry() > nowPlusPadding) return; // still valid

        log.info("[STRAVA] Refreshing token for user {}", user.getEmail());
        try {
            Map<String, String> body = new HashMap<>();
            body.put("client_id",     clientId);
            body.put("client_secret", clientSecret);
            body.put("grant_type",    "refresh_token");
            body.put("refresh_token", user.getStravaRefreshToken());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<Map> resp = rest.postForEntity(TOKEN_URL,
                    new HttpEntity<>(body, headers), Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<String, Object> data = resp.getBody();
                user.setStravaAccessToken((String) data.get("access_token"));
                user.setStravaRefreshToken((String) data.get("refresh_token"));
                Object exp = data.get("expires_at");
                if (exp instanceof Number) user.setStravaTokenExpiry(((Number) exp).longValue());
                userRepo.save(user);
                log.info("[STRAVA] Token refreshed successfully.");
            }
        } catch (Exception e) {
            log.error("[STRAVA] Token refresh failed: {}", e.getMessage());
        }
    }

    /**
     * Fetches up to {@code perPage} recent activities from the Strava API
     * and converts them to {@link WorkoutLog} objects (not yet persisted).
     */
    @SuppressWarnings("unchecked")
    public List<WorkoutLog> fetchActivities(User user, Member member, int perPage) {
        refreshIfNeeded(user);
        List<WorkoutLog> result = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(user.getStravaAccessToken());
            String url = ACTIVITIES_URL + "?per_page=" + perPage;

            ResponseEntity<List> resp = rest.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), List.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                for (Object item : resp.getBody()) {
                    if (item instanceof Map) {
                        WorkoutLog w = mapActivity((Map<String, Object>) item, member);
                        if (w != null) result.add(w);
                    }
                }
                log.info("[STRAVA] Fetched {} activities for {}", result.size(), user.getEmail());
            }
        } catch (Exception e) {
            log.error("[STRAVA] Fetch activities failed: {}", e.getMessage());
        }
        return result;
    }

    // ─── Mapping helpers ─────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private WorkoutLog mapActivity(Map<String, Object> a, Member member) {
        try {
            WorkoutLog w = new WorkoutLog();
            w.setMember(member);
            w.setSource("strava");

            // Use Strava activity ID as dedup key
            Object idObj = a.get("id");
            w.setHealthKitUUID("strava_" + (idObj != null ? idObj.toString() : UUID.randomUUID().toString()));

            // Type
            w.setWorkoutType(mapType((String) a.get("type")));

            // Start time
            String startDate = (String) a.get("start_date");
            if (startDate != null) {
                w.setStartTime(LocalDateTime.parse(
                    startDate.replace("Z", ""), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                w.setStartTime(LocalDateTime.now());
            }

            // Duration (Strava gives elapsed_time in seconds)
            Object elapsed = a.get("elapsed_time");
            if (elapsed instanceof Number) {
                int seconds = ((Number) elapsed).intValue();
                w.setDurationSeconds(seconds);
                w.setEndTime(w.getStartTime().plusSeconds(seconds));
            }

            // Distance (Strava gives metres)
            Object dist = a.get("distance");
            if (dist instanceof Number) w.setDistanceMeters(((Number) dist).doubleValue());

            // Calories
            Object cal = a.get("calories");
            if (cal instanceof Number) w.setActiveCalories(((Number) cal).doubleValue());

            // Heart rates
            Object avgHr = a.get("average_heartrate");
            if (avgHr instanceof Number) w.setAvgHeartRate(((Number) avgHr).intValue());
            Object maxHr = a.get("max_heartrate");
            if (maxHr instanceof Number) w.setMaxHeartRate(((Number) maxHr).intValue());

            // Notes: use Strava activity name
            Object name = a.get("name");
            if (name instanceof String) w.setNotes("Strava: " + name);

            return w;
        } catch (Exception e) {
            log.warn("[STRAVA] Could not map activity: {}", e.getMessage());
            return null;
        }
    }

    private WorkoutLog.WorkoutType mapType(String type) {
        if (type == null) return WorkoutLog.WorkoutType.OTHER;
        return switch (type) {
            case "Run", "TrailRun", "VirtualRun"                      -> WorkoutLog.WorkoutType.RUNNING;
            case "Ride","EBikeRide","VirtualRide","MountainBikeRide"  -> WorkoutLog.WorkoutType.CYCLING;
            case "Swim"                                               -> WorkoutLog.WorkoutType.SWIMMING;
            case "WeightTraining","Workout","Crossfit"                -> WorkoutLog.WorkoutType.STRENGTH;
            case "Yoga","Pilates"                                     -> WorkoutLog.WorkoutType.YOGA;
            case "Hike","Walk"                                        -> WorkoutLog.WorkoutType.CARDIO;
            case "HIIT","Interval"                                    -> WorkoutLog.WorkoutType.HIIT;
            default                                                   -> WorkoutLog.WorkoutType.OTHER;
        };
    }
}
