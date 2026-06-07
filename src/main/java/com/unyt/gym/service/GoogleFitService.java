package com.unyt.gym.service;

import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.User;
import com.unyt.gym.entity.WorkoutLog;
import com.unyt.gym.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Handles Google Fit OAuth2 authentication and activity data retrieval.
 *
 * <p>Flow:
 * <ol>
 *   <li>User clicks "Connect Google Fit" → redirected to {@link #getAuthorizationUrl()}</li>
 *   <li>User approves → Google calls our callback with a code</li>
 *   <li>We exchange the code for tokens via {@link #exchangeCode(String, User)}</li>
 *   <li>Tokens are stored on the {@link User} entity</li>
 *   <li>User clicks "Sync" → {@link #fetchActivities(User, Member, int)} fetches real workouts</li>
 * </ol>
 *
 * <p>Uses two Google Fit REST API calls per sync:
 * <ol>
 *   <li>Sessions list — activity name, type, start/end time</li>
 *   <li>Dataset aggregate — calories and average heart rate per session</li>
 * </ol>
 */
@Service
public class GoogleFitService {

    private static final Logger log = LoggerFactory.getLogger(GoogleFitService.class);

    private static final String AUTH_URL      = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL     = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL  = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String SESSIONS_URL  = "https://www.googleapis.com/fitness/v1/users/me/sessions";
    private static final String AGGREGATE_URL = "https://www.googleapis.com/fitness/v1/users/me/dataset:aggregate";

    @Value("${googlefit.client-id}")
    private String clientId;

    @Value("${googlefit.client-secret}")
    private String clientSecret;

    @Value("${googlefit.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepo;
    private final RestTemplate rest = new RestTemplate();

    public GoogleFitService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /** Builds the Google OAuth2 authorization URL. */
    public String getAuthorizationUrl() {
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(
            "openid email https://www.googleapis.com/auth/fitness.activity.read " +
            "https://www.googleapis.com/auth/fitness.heart_rate.read",
            StandardCharsets.UTF_8);
        return AUTH_URL
            + "?response_type=code"
            + "&client_id=" + clientId
            + "&redirect_uri=" + encodedRedirect
            + "&scope=" + encodedScope
            + "&access_type=offline"   // ensures a refresh token is returned
            + "&prompt=consent";       // forces refresh token even if previously authorized
    }

    /**
     * Exchanges an authorization code for access + refresh tokens.
     * Also fetches the user's email from the userinfo endpoint.
     *
     * @return the user's Google email, or null on failure
     */
    @SuppressWarnings("unchecked")
    public String exchangeCode(String code, User user) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type",    "authorization_code");
            body.add("code",          code);
            body.add("redirect_uri",  redirectUri);
            body.add("client_id",     clientId);
            body.add("client_secret", clientSecret);

            ResponseEntity<Map> resp = rest.postForEntity(TOKEN_URL,
                    new HttpEntity<>(body, headers), Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<String, Object> data = resp.getBody();
                user.setGoogleFitAccessToken((String) data.get("access_token"));

                // Google only sends refresh_token on first authorization
                String refreshToken = (String) data.get("refresh_token");
                if (refreshToken != null) user.setGoogleFitRefreshToken(refreshToken);

                Object expiresIn = data.get("expires_in");
                if (expiresIn instanceof Number) {
                    user.setGoogleFitTokenExpiry(
                        Instant.now().getEpochSecond() + ((Number) expiresIn).longValue());
                }

                // Fetch email from userinfo
                String email = fetchUserEmail(user.getGoogleFitAccessToken());
                user.setGoogleFitEmail(email);
                userRepo.save(user);
                log.info("[GOOGLEFIT] Token exchanged for {}", email);
                return email;
            }
        } catch (Exception e) {
            log.error("[GOOGLEFIT] Token exchange failed: {}", e.getMessage());
        }
        return null;
    }

    /** Fetches the authenticated user's email from Google's userinfo endpoint. */
    @SuppressWarnings("unchecked")
    private String fetchUserEmail(String accessToken) {
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(accessToken);
            ResponseEntity<Map> resp = rest.exchange(USERINFO_URL, HttpMethod.GET,
                    new HttpEntity<>(h), Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return (String) resp.getBody().get("email");
            }
        } catch (Exception e) {
            log.warn("[GOOGLEFIT] Could not fetch user email: {}", e.getMessage());
        }
        return null;
    }

    /** Refreshes the Google Fit access token if it is expired or about to expire. */
    @SuppressWarnings("unchecked")
    public void refreshIfNeeded(User user) {
        if (user.getGoogleFitTokenExpiry() == null) return;
        long nowPlusPadding = Instant.now().getEpochSecond() + 300;
        if (user.getGoogleFitTokenExpiry() > nowPlusPadding) return;
        if (user.getGoogleFitRefreshToken() == null) return;

        log.info("[GOOGLEFIT] Refreshing token for user {}", user.getEmail());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type",    "refresh_token");
            body.add("refresh_token", user.getGoogleFitRefreshToken());
            body.add("client_id",     clientId);
            body.add("client_secret", clientSecret);

            ResponseEntity<Map> resp = rest.postForEntity(TOKEN_URL,
                    new HttpEntity<>(body, headers), Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<String, Object> data = resp.getBody();
                user.setGoogleFitAccessToken((String) data.get("access_token"));
                Object expiresIn = data.get("expires_in");
                if (expiresIn instanceof Number) {
                    user.setGoogleFitTokenExpiry(
                        Instant.now().getEpochSecond() + ((Number) expiresIn).longValue());
                }
                userRepo.save(user);
                log.info("[GOOGLEFIT] Token refreshed successfully.");
            }
        } catch (Exception e) {
            log.error("[GOOGLEFIT] Token refresh failed: {}", e.getMessage());
        }
    }

    /**
     * Fetches up to {@code limit} recent sessions (last 30 days) from the Google Fit API
     * and enriches them with calorie and heart rate data from the aggregate endpoint.
     */
    @SuppressWarnings("unchecked")
    public List<WorkoutLog> fetchActivities(User user, Member member, int limit) {
        refreshIfNeeded(user);
        List<WorkoutLog> result = new ArrayList<>();

        try {
            Instant endTime   = Instant.now();
            Instant startTime = endTime.minus(30, ChronoUnit.DAYS);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(user.getGoogleFitAccessToken());

            // 1. Get sessions list
            String sessionsUrl = SESSIONS_URL
                + "?startTime=" + startTime.toString()
                + "&endTime="   + endTime.toString();

            ResponseEntity<Map> sessResp = rest.exchange(sessionsUrl, HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class);

            if (!sessResp.getStatusCode().is2xxSuccessful() || sessResp.getBody() == null) {
                log.warn("[GOOGLEFIT] Sessions API returned no data.");
                return result;
            }

            Object rawSessions = sessResp.getBody().get("session");
            if (!(rawSessions instanceof List<?> sessionList) || sessionList.isEmpty()) {
                log.info("[GOOGLEFIT] No sessions found for {}", user.getEmail());
                return result;
            }

            List<Map<String, Object>> sessions = (List<Map<String, Object>>) sessionList;
            if (sessions.size() > limit) sessions = sessions.subList(0, limit);

            // 2. Aggregate calories + HR per session
            Map<String, double[]> sessionStats = fetchAggregateStats(
                user.getGoogleFitAccessToken(), startTime, endTime);

            // 3. Map sessions → WorkoutLog
            for (Map<String, Object> session : sessions) {
                WorkoutLog w = mapSession(session, sessionStats, member);
                if (w != null) result.add(w);
            }

            log.info("[GOOGLEFIT] Fetched {} activities for {}", result.size(), user.getEmail());

        } catch (Exception e) {
            log.error("[GOOGLEFIT] Fetch activities failed: {}", e.getMessage());
        }
        return result;
    }

    /** Returns a map of sessionId → [calories, avgHeartRate] from the aggregate endpoint. */
    @SuppressWarnings("unchecked")
    private Map<String, double[]> fetchAggregateStats(String accessToken,
                                                       Instant startTime,
                                                       Instant endTime) {
        Map<String, double[]> stats = new HashMap<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("aggregateBy", List.of(
                Map.of("dataTypeName", "com.google.calories.expended"),
                Map.of("dataTypeName", "com.google.heart_rate.summary")
            ));
            body.put("bucketBySession", Map.of());
            body.put("startTimeMillis", String.valueOf(startTime.toEpochMilli()));
            body.put("endTimeMillis",   String.valueOf(endTime.toEpochMilli()));

            ResponseEntity<Map> aggResp = rest.postForEntity(AGGREGATE_URL,
                    new HttpEntity<>(body, headers), Map.class);

            if (!aggResp.getStatusCode().is2xxSuccessful() || aggResp.getBody() == null) return stats;

            Object rawBuckets = aggResp.getBody().get("bucket");
            if (!(rawBuckets instanceof List<?> bucketList)) return stats;

            for (Object bucketObj : bucketList) {
                if (!(bucketObj instanceof Map<?, ?> bucket)) continue;
                Map<String, Object> sess = (Map<String, Object>) bucket.get("session");
                if (sess == null) continue;
                String sessId = (String) sess.get("id");

                double calories = 0;
                double avgHr    = 0;

                Object rawDatasets = bucket.get("dataset");
                if (rawDatasets instanceof List<?> datasets) {
                    for (Object dsObj : datasets) {
                        if (!(dsObj instanceof Map<?, ?> ds)) continue;
                        String dsId = (String) ds.get("dataSourceId");
                        Object rawPoints = ds.get("point");
                        if (!(rawPoints instanceof List<?> points) || points.isEmpty()) continue;

                        for (Object ptObj : points) {
                            if (!(ptObj instanceof Map<?, ?> pt)) continue;
                            Object rawVals = pt.get("value");
                            if (!(rawVals instanceof List<?> vals) || vals.isEmpty()) continue;

                            if (dsId != null && dsId.contains("calories")) {
                                // Sum all calorie points
                                Object v = ((Map<?, ?>) vals.get(0)).get("fpVal");
                                if (v instanceof Number) calories += ((Number) v).doubleValue();

                            } else if (dsId != null && dsId.contains("heart_rate")) {
                                // Summary type: value[0]=min, value[1]=max, value[2]=avg
                                if (vals.size() >= 3) {
                                    Object v = ((Map<?, ?>) vals.get(2)).get("fpVal");
                                    if (v instanceof Number) avgHr = ((Number) v).doubleValue();
                                } else {
                                    Object v = ((Map<?, ?>) vals.get(0)).get("fpVal");
                                    if (v instanceof Number) avgHr = ((Number) v).doubleValue();
                                }
                            }
                        }
                    }
                }
                stats.put(sessId, new double[]{calories, avgHr});
            }
        } catch (Exception e) {
            log.warn("[GOOGLEFIT] Aggregate fetch failed: {}", e.getMessage());
        }
        return stats;
    }

    // ─── Mapping helpers ─────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private WorkoutLog mapSession(Map<String, Object> session,
                                  Map<String, double[]> stats,
                                  Member member) {
        try {
            WorkoutLog w = new WorkoutLog();
            w.setMember(member);
            w.setSource("google_fit");

            String sessId = (String) session.get("id");
            w.setHealthKitUUID("googlefit_" + (sessId != null ? sessId : UUID.randomUUID()));

            // Activity type
            Object actType = session.get("activityType");
            w.setWorkoutType(mapActivityType(actType instanceof Number ? ((Number) actType).intValue() : -1));

            // Start / end times (milliseconds since epoch as String)
            Object startMs = session.get("startTimeMillis");
            Object endMs   = session.get("endTimeMillis");
            if (startMs != null) {
                long ms = Long.parseLong(startMs.toString());
                w.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneOffset.UTC));
            } else {
                w.setStartTime(LocalDateTime.now());
            }
            if (endMs != null) {
                long ms = Long.parseLong(endMs.toString());
                w.setEndTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneOffset.UTC));
            }

            // Duration from start/end
            if (w.getStartTime() != null && w.getEndTime() != null) {
                long secs = java.time.Duration.between(w.getStartTime(), w.getEndTime()).getSeconds();
                if (secs > 0) w.setDurationSeconds((int) secs);
            }

            // Calories + HR from aggregate stats
            if (sessId != null && stats.containsKey(sessId)) {
                double[] sd = stats.get(sessId);
                if (sd[0] > 0) w.setActiveCalories(sd[0]);
                if (sd[1] > 0) w.setAvgHeartRate((int) sd[1]);
            }

            // Notes: use session name if available
            Object name = session.get("name");
            if (name instanceof String s && !s.isBlank()) {
                w.setNotes("Google Fit: " + s);
            }

            return w;
        } catch (Exception e) {
            log.warn("[GOOGLEFIT] Could not map session: {}", e.getMessage());
            return null;
        }
    }

    private WorkoutLog.WorkoutType mapActivityType(int activityType) {
        return switch (activityType) {
            case 8, 9, 56   -> WorkoutLog.WorkoutType.RUNNING;    // running, jogging, treadmill
            case 1, 17, 113 -> WorkoutLog.WorkoutType.CYCLING;    // biking, spinning, cycling
            case 82         -> WorkoutLog.WorkoutType.SWIMMING;
            case 97, 80     -> WorkoutLog.WorkoutType.STRENGTH;   // weight training, strength
            case 100, 42    -> WorkoutLog.WorkoutType.YOGA;       // yoga, pilates
            case 45, 48, 64 -> WorkoutLog.WorkoutType.HIIT;       // circuit, cross-training, interval
            case 7, 93, 83  -> WorkoutLog.WorkoutType.CARDIO;     // walking, walking, hiking
            default         -> WorkoutLog.WorkoutType.OTHER;
        };
    }
}
