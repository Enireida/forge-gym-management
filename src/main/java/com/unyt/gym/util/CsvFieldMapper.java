package com.unyt.gym.util;

import java.util.*;

/**
 * Maps CSV column headers from any fitness-tracker export to the canonical
 * field names used by WorkoutLog.
 *
 * <p>Supported sources (tested header formats):
 * <ul>
 *   <li><b>Samsung Health</b> — "Start Time", "Duration(s)", "Mean HR (bpm)",
 *       "Max HR (bpm)", "Calorie (kcal)", "Distance (m)", "Step count", "Activity type"</li>
 *   <li><b>Fitbit</b> — "Activities Type", "Date", "Duration" (HH:MM:SS),
 *       "Calories Burned", "Avg. Heart Rate", "Steps", "Distance"</li>
 *   <li><b>Garmin</b> — "Activity Type", "Date", "Time" (HH:MM:SS),
 *       "Calories", "Avg HR", "Max HR", "Distance", "Steps"</li>
 *   <li><b>Google Fit</b> — "Activity type", "Start time", "Duration",
 *       "Calories (kcal)", "Average heart rate (bpm)", "Max heart rate (bpm)",
 *       "Distance (m)", "Step count"</li>
 *   <li><b>Strava</b> — "Activity Date", "Activity Type", "Elapsed Time",
 *       "Distance", "Average Heart Rate", "Max Heart Rate", "Calories"</li>
 *   <li><b>Generic</b> — any column named with common English terms</li>
 * </ul>
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>Strip unit labels from parentheses: {@code "Mean HR (bpm)"} → {@code "Mean HR"}</li>
 *   <li>Lowercase, remove punctuation, collapse spaces/hyphens to underscores:
 *       → {@code "mean_hr"}</li>
 *   <li>Look up the result in the alias table → canonical field {@code "avg_hr"}</li>
 * </ol>
 *
 * <h3>Unit detection</h3>
 * Different devices store duration and distance in different units.
 * {@link #detectDurationUnit} and {@link #detectDistanceUnit} inspect the raw
 * header string (before stripping) to return the correct {@link DurationUnit} /
 * {@link DistanceUnit}.  The value parsers then convert everything to
 * <em>minutes</em> and <em>kilometres</em> respectively, matching what
 * {@code WorkoutLog} stores internally.
 *
 * @author Enireida Reci
 */
public final class CsvFieldMapper {

    // ── Canonical field keys ──────────────────────────────────────────────
    public static final String DATE     = "date";
    public static final String TYPE     = "type";
    public static final String DURATION = "duration";   // stored as minutes
    public static final String CALORIES = "calories";
    public static final String AVG_HR   = "avg_hr";
    public static final String MAX_HR   = "max_hr";
    public static final String MIN_HR   = "min_hr";
    public static final String DISTANCE = "distance";   // stored as km
    public static final String STEPS    = "steps";
    public static final String NOTES    = "notes";

    /** Time unit stored in a CSV duration column. */
    public enum DurationUnit { MINUTES, SECONDS, MILLISECONDS, HHMMSS }

    /** Distance unit stored in a CSV distance column. */
    public enum DistanceUnit { KM, METRES, MILES }

    // alias (normalised) → canonical field name
    private static final Map<String, String> ALIASES = new LinkedHashMap<>();

    static {
        // ── Date / start time ─────────────────────────────────────────
        addAliases(DATE,
                "date", "start_date", "start_time", "activity_date",
                "datetime", "date_time", "workout_date", "timestamp",
                "start", "started_at", "recorded_at", "activity_date_local");

        // ── Workout type ──────────────────────────────────────────────
        addAliases(TYPE,
                "type", "workout_type", "activity", "activity_type",
                "activity_name", "exercise_type", "sport", "exercise",
                "activities_type",  // Fitbit CSV export
                "activityname",     // Fitbit API
                "title");           // Garmin

        // ── Duration ─────────────────────────────────────────────────
        // Stored internally as MINUTES.  Unit is detected from the raw header
        // by detectDurationUnit(), or from the value format (HH:MM:SS has colons).
        addAliases(DURATION,
                "duration_minutes", "duration_min", "minutes",
                "duration",          // Samsung (s), Fitbit (HH:MM:SS), Google Fit (s)
                "elapsed_time",      // Strava (seconds)
                "moving_time",       // Strava (seconds)
                "active_duration",
                "workout_time", "exercise_time",
                "time",              // Garmin (HH:MM:SS)
                "total_time");

        // ── Calories ─────────────────────────────────────────────────
        addAliases(CALORIES,
                "calories", "active_calories", "kcal",
                "calorie",           // Samsung Health "Calorie (kcal)"
                "energy",
                "calories_burned",   // Fitbit
                "activity_calories", // Fitbit
                "total_calories", "cal",
                "energy_kcal", "energy_expenditure");

        // ── Average heart rate ────────────────────────────────────────
        addAliases(AVG_HR,
                "avg_heart_rate", "avg_hr", "average_heart_rate",
                "mean_heart_rate",
                "mean_hr",           // Samsung Health "Mean HR (bpm)"
                "heart_rate",        // generic
                "hr_average", "hr_avg", "hr_mean",
                "average_hr", "avg_bpm", "mean_bpm", "average_bpm",
                "bpm");

        // ── Max heart rate ────────────────────────────────────────────
        addAliases(MAX_HR,
                "max_heart_rate", "max_hr", "maximum_heart_rate",
                "peak_heart_rate",   // Fitbit
                "max_bpm", "hr_max",
                "peak_hr", "peak_bpm", "maximum_hr");

        // ── Min heart rate ────────────────────────────────────────────
        addAliases(MIN_HR,
                "min_heart_rate", "min_hr", "minimum_heart_rate",
                "min_bpm", "hr_min", "resting_hr");

        // ── Distance ─────────────────────────────────────────────────
        // Stored internally as KM.  Unit is detected from the raw header.
        addAliases(DISTANCE,
                "distance_km", "distance",
                "total_distance", "dist");

        // ── Steps ─────────────────────────────────────────────────────
        addAliases(STEPS,
                "steps", "step_count", "stepcount",
                "total_steps", "step", "steps_count");

        // ── Notes ─────────────────────────────────────────────────────
        addAliases(NOTES,
                "notes", "note", "description", "comment", "comments",
                "activity_description", "workout_notes", "memo");
    }

    private CsvFieldMapper() {}

    private static void addAliases(String canonical, String... aliases) {
        for (String a : aliases) ALIASES.putIfAbsent(a, canonical);
    }

    // ─────────────────────────────────────────────────────────────────
    //  Normalise  (public so it can be tested)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Normalises a raw CSV column header so it can be looked up in the alias table.
     *
     * <pre>
     * "Mean HR (bpm)"        → "mean_hr"
     * "Avg. Heart Rate"      → "avg_heart_rate"
     * "Duration(s)"          → "duration"
     * "Calories Burned"      → "calories_burned"
     * "Distance (km)"        → "distance"
     * "Start Time"           → "start_time"
     * " \"Activity Type\" "  → "activity_type"
     * </pre>
     */
    public static String normalise(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return raw.trim()
                .replaceAll("﻿", "")             // BOM at start of file
                .replaceAll("\"", "")                 // CSV-quoted headers
                .toLowerCase()
                .replaceAll("\\s*\\([^)]*\\)", "")   // strip "(anything)"  e.g. "(bpm)"
                .replaceAll("[.,'%#]", "")            // remove punctuation
                .replaceAll("[ \\-/]+", "_")          // spaces / hyphens → underscore
                .replaceAll("_+", "_")                // collapse duplicate underscores
                .replaceAll("^_|_$", "");             // trim leading/trailing underscores
    }

    /**
     * Resolves a raw header to a canonical field key, or {@code null} if unknown.
     */
    public static String resolve(String rawHeader) {
        String n = normalise(rawHeader);
        return n == null ? null : ALIASES.get(n);
    }

    /**
     * Builds a map of {@code canonical field → column index} from a header row.
     * Unrecognised columns are ignored.  When two columns map to the same field,
     * the first one wins.
     *
     * @param rawHeaders the raw (un-lowercased) header cells from the CSV
     * @return map of canonical field name → 0-based column index
     */
    public static Map<String, Integer> mapHeaders(String[] rawHeaders) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int i = 0; i < rawHeaders.length; i++) {
            String canon = resolve(rawHeaders[i]);
            if (canon != null) out.putIfAbsent(canon, i);
        }
        return out;
    }

    // ─────────────────────────────────────────────────────────────────
    //  Source detection  (optional — used for Strava distance units)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Detects the most likely export source from the header set.
     * Returns one of: "samsung_health", "fitbit", "garmin", "google_fit",
     * "strava", or "unknown".
     */
    public static String detectSource(String[] rawHeaders) {
        Set<String> n = new HashSet<>();
        for (String h : rawHeaders) {
            String norm = normalise(h);
            if (norm != null) n.add(norm);
        }
        if (n.contains("activity_id") && n.contains("elapsed_time"))  return "strava";
        if (n.contains("mean_hr"))                                      return "samsung_health";
        if (n.contains("activities_type") || n.contains("activityname")) return "fitbit";
        if (n.contains("time") && n.contains("avg_hr"))                return "garmin";
        if (n.contains("average_heart_rate") && n.contains("step_count")) return "google_fit";
        return "unknown";
    }

    // ─────────────────────────────────────────────────────────────────
    //  Unit detection
    // ─────────────────────────────────────────────────────────────────

    /**
     * Detects the time unit from a raw duration column header.
     *
     * <table>
     *   <tr><th>Raw header</th><th>Unit returned</th></tr>
     *   <tr><td>{@code Duration(s)}</td><td>SECONDS</td></tr>
     *   <tr><td>{@code Elapsed Time}</td><td>SECONDS</td></tr>
     *   <tr><td>{@code Moving Time}</td><td>SECONDS</td></tr>
     *   <tr><td>{@code Duration(ms)}</td><td>MILLISECONDS</td></tr>
     *   <tr><td>{@code duration_minutes}</td><td>MINUTES</td></tr>
     *   <tr><td>{@code Time}</td><td>HHMMSS (Garmin)</td></tr>
     *   <tr><td>{@code Duration} (no hint)</td><td>MINUTES — value parser detects HH:MM:SS from colons</td></tr>
     * </table>
     */
    public static DurationUnit detectDurationUnit(String rawHeader) {
        if (rawHeader == null) return DurationUnit.MINUTES;
        String low = rawHeader.toLowerCase();
        if (low.contains("(ms)") || low.contains(" ms)") || low.contains("milli"))
            return DurationUnit.MILLISECONDS;
        if (low.contains("(s)") || low.contains("_sec") || low.contains("elapsed")
                || low.contains("moving"))
            return DurationUnit.SECONDS;
        if (low.contains("min"))
            return DurationUnit.MINUTES;
        // Bare "time" (no further qualifiers) = Garmin's HH:MM:SS column
        String norm = normalise(low);
        if ("time".equals(norm))
            return DurationUnit.HHMMSS;
        return DurationUnit.MINUTES;
    }

    /**
     * Detects the distance unit from a raw distance column header.
     *
     * <table>
     *   <tr><th>Raw header</th><th>Unit returned</th></tr>
     *   <tr><td>{@code Distance (m)}</td><td>METRES  (Samsung Health, Google Fit)</td></tr>
     *   <tr><td>{@code Distance (km)}</td><td>KM</td></tr>
     *   <tr><td>{@code Distance (mi)}</td><td>MILES</td></tr>
     *   <tr><td>{@code Distance} (Strava detected via detectSource)</td><td>METRES</td></tr>
     *   <tr><td>default</td><td>KM</td></tr>
     * </table>
     */
    public static DistanceUnit detectDistanceUnit(String rawHeader, String source) {
        if (rawHeader == null) return DistanceUnit.KM;
        String low = rawHeader.toLowerCase();
        if (low.contains("(mi)") || low.contains("miles") || low.contains(" mi)"))
            return DistanceUnit.MILES;
        if (low.contains("(km)") || low.contains(" km)"))
            return DistanceUnit.KM;
        if (low.contains("(m)") || low.endsWith(" m)") || low.contains(" (m)"))
            return DistanceUnit.METRES;
        // Strava exports distance in metres with no unit in the header
        if ("strava".equals(source) || "samsung_health".equals(source) || "google_fit".equals(source))
            return DistanceUnit.METRES;
        return DistanceUnit.KM;
    }

    // ─────────────────────────────────────────────────────────────────
    //  Value parsers
    // ─────────────────────────────────────────────────────────────────

    /**
     * Converts a raw duration string to <b>integer minutes</b>.
     *
     * <ul>
     *   <li>{@code "1:30:00"} (any unit) → 90 min</li>
     *   <li>{@code "45:00"} → 45 min</li>
     *   <li>{@code "2700"} + SECONDS → 45 min</li>
     *   <li>{@code "2700000"} + MILLISECONDS → 45 min</li>
     *   <li>{@code "45"} + MINUTES → 45 min</li>
     * </ul>
     */
    public static Integer parseDurationAsMinutes(String raw, DurationUnit unit) {
        if (raw == null || raw.isBlank()) return null;
        raw = raw.trim().replace("\"", "");

        // HH:MM:SS or MM:SS — always takes priority regardless of unit hint
        if (raw.contains(":")) {
            try {
                String[] parts = raw.split(":");
                int totalSeconds = 0;
                if (parts.length == 3)
                    totalSeconds = Integer.parseInt(parts[0].trim()) * 3600
                                 + Integer.parseInt(parts[1].trim()) * 60
                                 + Integer.parseInt(parts[2].trim());
                else if (parts.length == 2)
                    totalSeconds = Integer.parseInt(parts[0].trim()) * 60
                                 + Integer.parseInt(parts[1].trim());
                return totalSeconds / 60;
            } catch (NumberFormatException e) { return null; }
        }

        // Numeric value — apply unit
        double val;
        try { val = Double.parseDouble(raw.replaceAll("[^\\d.\\-]", "")); }
        catch (NumberFormatException e) { return null; }

        return switch (unit) {
            case SECONDS      -> (int) Math.round(val / 60.0);
            case MILLISECONDS -> (int) Math.round(val / 60_000.0);
            default           -> (int) Math.round(val);  // MINUTES / HHMMSS fallback
        };
    }

    /**
     * Converts a raw distance string to <b>kilometres</b>.
     *
     * <ul>
     *   <li>{@code "5000"} + METRES → 5.0 km</li>
     *   <li>{@code "3.1"} + MILES → ~4.99 km</li>
     *   <li>{@code "5.0"} + KM → 5.0 km</li>
     * </ul>
     */
    public static Double parseDistanceAsKm(String raw, DistanceUnit unit) {
        if (raw == null || raw.isBlank()) return null;
        double val;
        try { val = Double.parseDouble(raw.trim().replace("\"", "").replaceAll("[^\\d.\\-]", "")); }
        catch (NumberFormatException e) { return null; }
        return switch (unit) {
            case METRES -> val / 1_000.0;
            case MILES  -> val * 1.60934;
            default     -> val;   // KM
        };
    }
}
