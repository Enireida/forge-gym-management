package com.unyt.gym.jdbc;

import com.unyt.gym.exception.GymException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure JDBC reporting layer.
 *
 * <p>Spring Data JPA covers most of the persistence (R7) but the
 * requirement document explicitly asks for raw JDBC with full CRUD (R6).
 * This class therefore performs every CRUD verb directly against the
 * underlying {@link DataSource}, demonstrating prepared statements,
 * result-set iteration, and explicit connection management.</p>
 *
 * <p>Spring also injects a {@link JdbcTemplate} which is used in the
 * report-style queries to keep the boilerplate down.</p>
 *
 * <p><b>Course requirement:</b> R6 JDBC / Database — at least CRUD.</p>
 */
@Repository
public class RawReportDao {

    private static final Logger log = LoggerFactory.getLogger(RawReportDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbc;

    @Autowired
    public RawReportDao(DataSource dataSource, JdbcTemplate jdbc) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
    }

    /* ============================================================
       Pure JDBC CRUD on an audit-log table created on first use.
       ============================================================ */

    /**
     * (C)reate the audit_log table the first time the application boots.
     * Using {@code CREATE TABLE IF NOT EXISTS} keeps the call idempotent.
     */
    public void ensureAuditTable() {
        String ddl = """
                CREATE TABLE IF NOT EXISTS audit_log (
                    id IDENTITY PRIMARY KEY,
                    actor VARCHAR(120) NOT NULL,
                    action VARCHAR(60) NOT NULL,
                    detail VARCHAR(500),
                    at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement()) {
            st.execute(ddl);
        } catch (SQLException e) {
            throw new GymException("Could not create audit_log table", e);
        }
    }

    /**
     * (C)reate audit row using a prepared statement and {@code RETURN_GENERATED_KEYS}.
     */
    public long insertAudit(String actor, String action, String detail) {
        String sql = "INSERT INTO audit_log(actor, action, detail) VALUES (?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, actor);
            ps.setString(2, action);
            ps.setString(3, detail);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1L;
            }
        } catch (SQLException e) {
            throw new GymException("insertAudit failed", e);
        }
    }

    /** (R)ead — most recent {@code limit} audit rows. */
    public List<Map<String, Object>> recentAudit(int limit) {
        String sql = "SELECT id, actor, action, detail, at FROM audit_log ORDER BY at DESC LIMIT ?";
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("id", rs.getLong("id"));
                    r.put("actor", rs.getString("actor"));
                    r.put("action", rs.getString("action"));
                    r.put("detail", rs.getString("detail"));
                    r.put("at", rs.getTimestamp("at"));
                    rows.add(r);
                }
            }
        } catch (SQLException e) {
            throw new GymException("recentAudit failed", e);
        }
        return rows;
    }

    /** (U)pdate — flag a row's detail field. */
    public int updateAuditDetail(long id, String newDetail) {
        String sql = "UPDATE audit_log SET detail = ? WHERE id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newDetail);
            ps.setLong(2, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new GymException("updateAuditDetail failed", e);
        }
    }

    /** (D)elete — purge audit rows older than the cut-off. */
    public int purgeAuditBefore(LocalDate cutoff) {
        String sql = "DELETE FROM audit_log WHERE at < ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(cutoff));
            int affected = ps.executeUpdate();
            log.info("Purged {} audit rows older than {}", affected, cutoff);
            return affected;
        } catch (SQLException e) {
            throw new GymException("purgeAuditBefore failed", e);
        }
    }

    /* ============================================================
       Convenience aggregates via JdbcTemplate.
       ============================================================ */

    /** Quick KPI: total revenue across all payments. */
    public BigDecimal totalRevenue() {
        BigDecimal v = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM payments", BigDecimal.class);
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Quick KPI: total active members. */
    public int activeMembers() {
        Integer v = jdbc.queryForObject(
                "SELECT COUNT(*) FROM members WHERE status = 'ACTIVE'", Integer.class);
        return v == null ? 0 : v;
    }

    /** Total check-ins today. */
    public int checkinsToday() {
        Integer v = jdbc.queryForObject(
                "SELECT COUNT(*) FROM attendance WHERE date = CURRENT_DATE", Integer.class);
        return v == null ? 0 : v;
    }

    /** Check-ins grouped by day for the last N days. */
    public Map<String, Integer> checkInsByDay(int days) {
        String sql = "SELECT CAST(date AS VARCHAR) AS d, COUNT(*) AS cnt " +
                     "FROM attendance " +
                     "WHERE date >= DATEADD('DAY', ?, CURRENT_DATE) " +
                     "GROUP BY date ORDER BY date";
        Map<String, Integer> result = new java.util.LinkedHashMap<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, -days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.put(rs.getString("d"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            log.warn("checkInsByDay failed: {}", e.getMessage());
        }
        return result;
    }

    /** New members grouped by month (YYYY-MM). */
    public Map<String, Integer> newMembersByMonth() {
        String sql = "SELECT FORMATDATETIME(created_at,'yyyy-MM') AS m, COUNT(*) AS cnt " +
                     "FROM members GROUP BY FORMATDATETIME(created_at,'yyyy-MM') ORDER BY m";
        Map<String, Integer> result = new java.util.LinkedHashMap<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.put(rs.getString("m"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            log.warn("newMembersByMonth failed: {}", e.getMessage());
        }
        return result;
    }

    /** Members grouped by plan name. */
    public Map<String, Integer> membersByPlan() {
        String sql = "SELECT p.name AS plan_name, COUNT(m.id) AS cnt " +
                     "FROM members m LEFT JOIN membership_plans p ON m.plan_id = p.id " +
                     "WHERE m.status = 'ACTIVE' " +
                     "GROUP BY p.name ORDER BY cnt DESC";
        Map<String, Integer> result = new java.util.LinkedHashMap<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.put(
                    rs.getString("plan_name") != null ? rs.getString("plan_name") : "No Plan",
                    rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            log.warn("membersByPlan failed: {}", e.getMessage());
        }
        return result;
    }
}
