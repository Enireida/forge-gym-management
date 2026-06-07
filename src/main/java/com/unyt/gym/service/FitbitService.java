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
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handles Fitbit OAuth2 authentication and activity data retrieval.
 *
 * <p>Flow:
 * <ol>
 *   <li>User clicks "Connect Fitbit" → redirected to {@link #getAuthorizationUrl()}</li>
 *   <li>User approves → Fitbit calls our callback with a code</li>
 *   <li>We exchange the code for tokens via {@link #exchangeCode(String, User)}</li>
 *   <li>Tokens are stored on the {@link User} entity</li>
 *   <li>User clicks "Sync" → {@link #fetchActivities(User, Member, int)} fetches real workouts</li>
 * </ol>
 *
 * <p>Unlike Strava, Fitbit token exchange uses HTTP Basic Auth
 * (Base64-encoded clientId:clientSecret) rather than a JSON body.
 */
@Service
public class FitbitService {

    private static final Logger log = LoggerFactory.getLogger(FitbitService.class);

    private static final String AUTH_URL       = "https://www.fitbit.com/oauth2/authorize";
    private static final String TOKEN_URL      = "https://api.fitbit.com/oauth2/token";
    private static final String ACTIVITIES_URL = "https://api.fitbit.com/1/user/-/activities/list.json";

    @Value("${fitbit.client-id}")
    private String clientId;

    @Value("${fitbit.client-secret}")
    private String clientSecret;

    @Value("${fitbit.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepo;
    private final RestTemplate rest = new RestTemplate();

    public FitbitService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /** Builds the Fitbit OAuth2 authorization URL. */
    public String getAuthorizationUrl() {
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return AUTH_URL
            + "?response_type=code"
            + "&client_id=" + clientId
            + "&redirect_uri=" + encodedRedirect
            + "&scope=activity%20heartrate%20profile"
            + "&expires_in=604800";
    }

    /**
     * Exchanges an authorization code for access + refresh tokens.
     * Fitbit requires Basic Auth on the token endpoint (not a JSON body).
     *
     * @return the Fitbit user ID, or null on failure
     */
    @SuppressWarnings("unchecked")
    public String exchangeCode(String code, User user) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId, clientSecret);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type",   "authorization_code");
            body.add("code",         code);
            body.add("redirect_uri", redirectUri);

            ResponseEntity<Map> resp = rest.postForEntity(TOKEN_URL,
                    new HttpEntity<>(body, headers), Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<String, Object> data = resp.getBody();
                user.setFitbitAccessToken((String) data.get("access_token"));
                user.setFitbitRefreshToken((String) data.get("refresh_token"));

                // Fitbit returns expires_in (seconds from now), convert to Unix epoch
                Object expiresIn = data.get("expires_in");
                if (expiresIn instanceof Number) {
                    user.setFitbitTokenExpiry(
                        Instant.now().getEpochSecond() + ((Number) expiresIn).longValue());
                }

                String fitbitUserId = (String) data.get("user_id");
                user.setFitbitUserId(fitbitUserId);
                userRepo.save(user);
                log.info("[FITBIT] Token exchanged for user {}", fitbitUserId);
                return fitbitUserId;
            }
        } catch (Exception e) {
            log.error("[FITBIT] Token exchange failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Refreshes the Fitbit access token if it is expired or about to expire.
     */
    @SuppressWarnings("unchecked")
    public void refreshIfNeeded(User user) {
        if (user.getFitbitTokenExpiry() == null) return;
        long nowPlusPadding = Instant.now().getEpochSecond() + 300;
        if (user.getFitbitTokenExpiry() > nowPlusPadding) return;

        log.info("[FITBIT] Refreshing token for user {}", user.getEmail());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId, clientSecret);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type",    "refresh_token");
            body.add("refresh_token", user.getFitbitRefreshToken());

            ResponseEntity<Map> resp = rest.postForEntity(TOKEN_URL,
                    new HttpEntity<>(body, headers), Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<String, Object> data = resp.getBody();
                user.setFitbitAccessToken((String) data.get("access_token"));
                user.setFitbitRefreshToken((String) data.get("refresh_token"));
                Object expiresIn = data.get("expires_in");
                if (expiresIn instanceof Number) {
                    user.setFitbitTokenExpiry(
                        Instant.now().getEpochSecond() + ((Number) expiresIn).longValue());
                }
                userRepo.save(user);
                log.info("[FITBIT] Token refreshed successfully.");
            }
        } catch (Exception e) {
            log.error("[FITBIT] Token refresh failed: {}", e.getMessage());
        }
    }

    /**
     * Fetches up to {@code limit} recent activities from the Fitbit API
     * and converts them to {@link WorkoutLog} objects (not yet persisted).
     * Looks back 30 days.
     */
    @SuppressWarnings("unchecked")
    public List<WorkoutLog> fetchActivities(User user, Member member, int limit) {
        refreshIfNeeded(user);
        List<WorkoutLog> result = new ArrayList<>();
        try {
            String afterDate = LocalDate.now().minusDays(30)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(user.getFitbitAccessToken());

            String url = ACTIVITIES_URL
                + "?afterDate=" + afterDate
                + "&sort=desc&offset=0&limit=" + limit;

            ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object activitiesObj = resp.getBody().get("activities");
                if (activitiesObj instanceof List<?> activitiesList) {
                    for (Object item : activitiesList) {
                        if (item instanceof Map) {
                            WorkoutLog w = mapActivity((Map<String, Object>) item, member);
                            if (w != null) result.add(w);
                        }
                    }
                }
                log.info("[FITBIT] Fetched {} activities for {}", result.size(), user.getEmail());
            }
        } catch (Exception e) {
            log.error("[FITBIT] Fetch activities failed: {}", e.getMessage());
        }
        return result;
    }

    // ─── Mapping helpers ─────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private WorkoutLog mapActivity(Map<String, Object> a, Member member) {
        try {
            WorkoutLog w = new WorkoutLog();
            w.setMember(member);
            w.setSource("fitbit");

            // Dedup key using Fitbit logId
            Object logId = a.get("logId");
            w.setHealthKitUUID("fitbit_" + (logId != null ? logId.toString() : UUID.randomUUID()));

            // Type
            w.setWorkoutType(mapType((String) a.get("activityName")));

            // Start time — Fitbit gives startDate ("2024-01-15") and startTime ("06:30") separately
            String startDate = (String) a.get("startDate");
            String startTime = (String) a.get("startTime");
            if (startDate != null && startTime != null) {
                w.setStartTime(LocalDateTime.parse(startDate + "T" + startTime,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            } else {
                w.setStartTime(LocalDateTime.now());
            }

            // Duration — Fitbit gives milliseconds
            Object durationMs = a.get("duration");
            if (durationMs instanceof Number) {
                int seconds = (int) (((Number) durationMs).longValue() / 1000);
                w.setDurationSeconds(seconds);
                w.setEndTime(w.getStartTime().plusSeconds(seconds));
            }

            // Distance — Fitbit gives km (for metric-locale users); store as metres
            Object dist = a.get("distance");
            if (dist instanceof Number) {
                w.setDistanceMeters(((Number) dist).doubleValue() * 1000.0);
            }

            // Calories
            Object cal = a.get("calories");
            if (cal instanceof Number) w.setActiveCalories(((Number) cal).doubleValue());

            // Heart rate (may be absent if HR not enabled)
            Object avgHr = a.get("averageHeartRate");
            if (avgHr instanceof Number) w.setAvgHeartRate(((Number) avgHr).intValue());

            // Notes
            Object name = a.get("activityName");
            if (name instanceof String) w.setNotes("Fitbit: " + name);

            return w;
        } catch (Exception e) {
            log.warn("[FITBIT] Could not map activity: {}", e.getMessage());
            return null;
        }
    }

    private WorkoutLog.WorkoutType mapType(String name) {
        if (name == null) return WorkoutLog.WorkoutType.OTHER;
        String n = name.toLowerCase();
        if (n.contains("run") || n.contains("jog"))                return WorkoutLog.WorkoutType.RUNNING;
        if (n.contains("bike") || n.contains("cycl") || n.contains("spin")) return WorkoutLog.WorkoutType.CYCLING;
        if (n.contains("swim"))                                      return WorkoutLog.WorkoutType.SWIMMING;
        if (n.contains("weight") || n.contains("strength") || n.contains("lift")) return WorkoutLog.WorkoutType.STRENGTH;
        if (n.contains("yoga") || n.contains("pilates"))             return WorkoutLog.WorkoutType.YOGA;
        if (n.contains("hiit") || n.contains("interval"))           return WorkoutLog.WorkoutType.HIIT;
        if (n.contains("walk") || n.contains("hike") || n.contains("cardio")) return WorkoutLog.WorkoutType.CARDIO;
        return WorkoutLog.WorkoutType.OTHER;
    }
}
