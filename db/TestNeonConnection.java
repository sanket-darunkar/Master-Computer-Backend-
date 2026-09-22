/**
 * Neon PostgreSQL connection tester.
 *
 * USAGE (run in your own terminal with NEON_DATABASE_URL already exported):
 *
 *   cd db
 *   javac -cp ../target/certificate-verification-1.0.0.jar TestNeonConnection.java
 *   java  -cp .:../target/certificate-verification-1.0.0.jar TestNeonConnection
 *
 * The connection string is read from the NEON_DATABASE_URL environment variable.
 * It is NEVER printed, logged, or displayed.
 *
 * Reports only SUCCESS or FAILURE.
 */

import java.sql.*;

public class TestNeonConnection {

    public static void main(String[] args) {
        String rawUrl = System.getenv("NEON_DATABASE_URL");

        if (rawUrl == null || rawUrl.isBlank()) {
            System.err.println("ERROR: NEON_DATABASE_URL environment variable is not set.");
            System.err.println("  Export it in your terminal first:");
            System.err.println("    export NEON_DATABASE_URL=jdbc:postgresql://...");
            System.exit(1);
        }

        // Convert postgresql:// → jdbc:postgresql:// if the raw Neon URL
        // was copied directly from the dashboard (non-JDBC format).
        String jdbcUrl = rawUrl.startsWith("jdbc:") ? rawUrl
                : "jdbc:" + rawUrl;

        // Neon requires SSL – append sslmode=require if not present
        if (!jdbcUrl.contains("sslmode=")) {
            jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
        }

        System.out.println("Attempting connection to Neon PostgreSQL...");
        System.out.println("  (Connection string is NOT printed for security)");

        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery("SELECT version()")) {

            if (rs.next()) {
                // Print only the PG version string – no credential information
                System.out.println("  PostgreSQL version: " + rs.getString(1));
            }
            System.out.println();
            System.out.println("CONNECTION: SUCCESS");

        } catch (SQLException e) {
            System.err.println();
            System.err.println("CONNECTION: FAILED");
            // Print error class only – never print the URL or credentials
            System.err.println("  SQLState : " + e.getSQLState());
            System.err.println("  Message  : " + e.getMessage()
                    // Redact anything that looks like a password or token
                    .replaceAll("(?i)(password|token|secret|npg_[A-Za-z0-9]+)=?[^\\s,;]*", "[REDACTED]"));
            System.exit(1);
        }
    }
}
