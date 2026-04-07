import java.sql.*;
import java.util.*;

public class CRUD {

    // ================= DB CONNECTION =================
    public static Connection getConnection() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");

        String url = "jdbc:oracle:thin:@localhost:1521/XEPDB1";
            String user = "System";     
            String password = "1234";

        Connection conn = DriverManager.getConnection(url, user, password);
        return conn;
    }

    // ================= ADD BUG (FROM UI) =================
public static void addBugFromUI(String title, String description,
                                String artifactType, String reportedBy,
                                String priority) throws Exception {

    String sql = "INSERT INTO system.bugs " +
                 "(title, description, artifact_type, status, priority, reported_by) " +
                 "VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, title);
        ps.setString(2, description);
        ps.setString(3, (artifactType == null || artifactType.trim().isEmpty()) ? "Unknown" : artifactType);
        ps.setString(4, "OPEN");
        ps.setString(5, priority);
        ps.setString(6, reportedBy);

        ps.executeUpdate();

    } catch (SQLException e) {
        // ORA-00001: unique constraint violated
        if (e.getErrorCode() == 1) {
            throw new Exception("Title already exists. Please enter a new title.");
        } 
        // ORA-01400: cannot insert null
    else if (e.getErrorCode() == 1400) {
        throw new Exception("Please fill out all empty fields.");
    }
        // ORA-02291: foreign key (reporter not found)
        else if (e.getErrorCode() == 2291 && e.getMessage().contains("FK_REPORTER")) {
            throw new Exception("User not found in system");
        }else {
            throw e; // rethrow other SQL exceptions
        }
    }
}

    
    // ================= GET ALL BUGS =================
public static String getAllBugsText() throws Exception {

    StringBuilder sb = new StringBuilder();

    String sql = "SELECT bug_id, title, description, artifact_type, priority, status, reported_by " +
                 "FROM system.bugs ORDER BY bug_id";

    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        // Header
        sb.append(String.format("%-5s %-20s %-30s %-15s %-10s %-10s %-15s\n",
                "ID", "Title", "Description", "Artifact", "Priority", "Status", "Reporter"));

        sb.append("---------------------------------------------------------------------------------------------------------------\n");

        // Rows
        while (rs.next()) {

            String title = safe(rs.getString("title"), 18);
            String desc = safe(rs.getString("description"), 28);
            String artifact = safe(rs.getString("artifact_type"), 13);
            String priority = safe(rs.getString("priority"), 8);
            String status = safe(rs.getString("status"), 8);
            String reported_by = safe(rs.getString("reported_by"), 13);

            sb.append(String.format("%-5d %-20s %-30s %-15s %-10s %-10s %-15s\n",
                    rs.getInt("bug_id"),
                    title,
                    desc,
                    artifact,
                    priority,
                    status,
                    reported_by
            ));
        }
    }

    return sb.toString();
}

private static String safe(String value, int maxLength) {
    if (value == null) return "N/A";

    if (value.length() > maxLength) {
        return value.substring(0, maxLength - 2) + "..";
    }

    return value;
}


    // ================ BUG TABLE VIEW ===============
    public static java.util.List<Bug> getAllBugs() throws Exception {
    java.util.List<Bug> bugs = new java.util.ArrayList<>();

    String sql = "SELECT bug_id, title, description, artifact_type, priority, status, reported_by, " +
             "date_found, date_fixed " +
             "FROM system.bugs ORDER BY bug_id";

    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            bugs.add(new Bug(
                rs.getInt("bug_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("artifact_type"),
                rs.getString("priority"),
                rs.getString("status"),
                rs.getString("reported_by"),
    rs.getDate("date_found"),
    rs.getDate("date_fixed")
));
        }
    }

    return bugs;
}
    // ================= SEARCH BUGS =================
    public static String searchBugText(String keyword) throws Exception {

        StringBuilder sb = new StringBuilder();

        String sql = "SELECT bug_id, title, artifact_type, priority, status " +
                     "FROM system.bugs " +
                     "WHERE LOWER(title) LIKE ? OR LOWER(description) LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String search = "%" + keyword.toLowerCase() + "%";

            ps.setString(1, search);
            ps.setString(2, search);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                sb.append(rs.getInt("bug_id")).append(" | ")
                  .append(rs.getString("title")).append(" | ")
                  .append(rs.getString("artifact_type")).append(" | ")
                  .append(rs.getString("priority")).append(" | ")
                  .append(rs.getString("status")).append("\n");
            }
        }

        return sb.toString();
    }

    // ================= GET ALL UNIQUE STATUSES =================
    public static java.util.List<String> getAllStatuses() throws Exception {
        java.util.List<String> statuses = new java.util.ArrayList<>();

        String sql = "SELECT DISTINCT status FROM system.bugs ORDER BY status";

        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                statuses.add(rs.getString("status"));
            }
        }

        return statuses;
    }
    
    // ================= GET ALL UNIQUE ARTIFACT TYPES =================
    public static java.util.List<String> getAllArtifactTypes() throws Exception {
    java.util.List<String> artifacts = new java.util.ArrayList<>();

    String sql = "SELECT DISTINCT artifact_type FROM system.bugs ORDER BY artifact_type";

    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            artifacts.add(rs.getString("artifact_type"));
        }
    }

    return artifacts;
}

// ================= GET ALL UNIQUE REPORTER ID AND THE NAME ASSOCIATED =================
public static Map<String, String> getReporterMap() throws Exception {
    Map<String, String> map = new HashMap<>();
    String sql = "SELECT reporter_id, name FROM system.reporters ORDER BY name";
    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            map.put(rs.getString("reporter_id"), rs.getString("name"));
        }
    }
    return map;
}

// ================= GET BUGS BY FILTERS =================
public static String getBugsByFilters(String status, String artifact, String reporter) throws Exception {
    StringBuilder sb = new StringBuilder();

    StringBuilder sql = new StringBuilder(
        "SELECT bug_id, title, artifact_type, priority, status " +
        "FROM system.bugs WHERE 1=1"
    );

    java.util.List<String> params = new java.util.ArrayList<>();

    // ===== FILTERS =====
    if (status != null && !status.equalsIgnoreCase("All")) {
        sql.append(" AND status = ?");
        params.add(status);
    }

    if (artifact != null && !artifact.equalsIgnoreCase("All")) {
        sql.append(" AND artifact_type = ?");
        params.add(artifact);
    }

    if (reporter != null && !reporter.equalsIgnoreCase("All")) {
        sql.append(" AND reported_by = ?");
        params.add(reporter);
    }

    sql.append(" ORDER BY bug_id");

    // ===== EXECUTION =====
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql.toString())) {

        for (int i = 0; i < params.size(); i++) {
            ps.setString(i + 1, params.get(i));
        }

        // ✅ ResultSet inside try (important)
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                sb.append(rs.getInt("bug_id")).append(" | ")
                  .append(rs.getString("title")).append(" | ")
                  .append(rs.getString("artifact_type")).append(" | ")
                  .append(rs.getString("priority")).append(" | ")
                  .append(rs.getString("status")).append("\n");
            }
        }
    }

    // ✅ Handle empty results (UX fix)
    if (sb.length() == 0) {
        return "No bugs found for selected filters.";
    }

    return sb.toString();
}

    // ================= GET ALL BUG TITLES =================
    public static java.util.List<String> getAllTitles() throws Exception {
    java.util.List<String> titles = new java.util.ArrayList<>();

    String sql = "SELECT DISTINCT title FROM system.bugs ORDER BY title";

    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            titles.add(rs.getString("title"));
        }
    }

    return titles;
}

    // ================= DELETE BUG BY TITLE =================
    public static String deleteBugByTitle(String title) throws Exception {
    String sql = "DELETE FROM system.bugs WHERE title = ?";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, title);
        int rows = ps.executeUpdate();

        return rows > 0 ? "Bug deleted." : "Title not found.";
    }
}

    // ================= UPDATE STATUS =================
    //when resolved selected, add todays date into date fixed column
public static String updateStatus(String title, String status) throws Exception {

    String sql = "UPDATE system.bugs " +
                 "SET status = ?, date_fixed = ? " +
                 "WHERE title = ?";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, status);

        if ("Resolved".equalsIgnoreCase(status)) {
            ps.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now()));
        } else {
            ps.setNull(2, java.sql.Types.DATE); // or keep existing date (see below)
        }

        ps.setString(3, title);

        int rows = ps.executeUpdate();

        return rows > 0 ? "Status updated." : "Bug not found.";
    }
}

    // bug table update
    public static java.util.List<Bug> searchBugObjects(String keyword) throws Exception {
    java.util.List<Bug> bugs = new java.util.ArrayList<>();

    String sql = "SELECT bug_id, title, description, artifact_type, priority, status, reported_by " +
                 "FROM system.bugs " +
                 "WHERE LOWER(title) LIKE ? OR LOWER(description) LIKE ?";

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        String search = "%" + keyword.toLowerCase() + "%";
        stmt.setString(1, search);
        stmt.setString(2, search);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            bugs.add(new Bug(
                rs.getInt("bug_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("artifact_type"),
                rs.getString("priority"),
                rs.getString("status"),
                rs.getString("reported_by"),
                rs.getDate("date_found"),
    rs.getDate("date_fixed")
            ));
        }
    }

    return bugs;
}

    // ================= UPDATE DESCRIPTION =================
    public static String updateDescription(String title, String newDesc) throws Exception {

    String sql = "UPDATE system.bugs SET description=? WHERE title=?";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, newDesc);
        ps.setString(2, title);  // <- fixed here

        int rows = ps.executeUpdate();

        return rows > 0 ? "Description updated." : "Bug title not found.";
    }
}
// debug to check artifact


    
}