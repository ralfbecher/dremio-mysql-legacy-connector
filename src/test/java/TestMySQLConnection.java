import java.sql.*;
import java.util.Properties;

/**
 * Simple test to verify MySQL JDBC driver can list databases/catalogs.
 *
 * Usage: java -cp mysql-connector-java-5.1.49.jar:. TestMySQLConnection <host> <port> <user> <password>
 */
public class TestMySQLConnection {

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage: java TestMySQLConnection <host> <port> <user> <password>");
            System.exit(1);
        }

        String host = args[0];
        String port = args[1];
        String user = args[2];
        String password = args[3];

        // Try both driver class names
        String[] drivers = {
            "com.mysql.jdbc.Driver",      // MySQL Connector/J 5.1.x
            "com.mysql.cj.jdbc.Driver"    // MySQL Connector/J 8.x
        };

        for (String driver : drivers) {
            try {
                Class.forName(driver);
                System.out.println("✓ Loaded driver: " + driver);
                break;
            } catch (ClassNotFoundException e) {
                System.out.println("✗ Driver not found: " + driver);
            }
        }

        String url = String.format("jdbc:mysql://%s:%s?useSSL=false&zeroDateTimeBehavior=convertToNull", host, port);
        System.out.println("\nConnecting to: " + url);

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✓ Connected successfully!\n");

            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Driver: " + meta.getDriverName() + " " + meta.getDriverVersion());
            System.out.println("Database: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            System.out.println();

            // Test 1: List catalogs (MySQL databases)
            String firstCatalog = null;
            System.out.println("=== CATALOGS (getCatalogs) ===");
            try (ResultSet rs = meta.getCatalogs()) {
                int count = 0;
                while (rs.next()) {
                    String cat = rs.getString("TABLE_CAT");
                    System.out.println("  " + cat);
                    if (firstCatalog == null && !cat.equals("information_schema") && !cat.equals("mysql") && !cat.equals("performance_schema") && !cat.equals("sys")) {
                        firstCatalog = cat;
                    }
                    count++;
                }
                System.out.println("Total: " + count + " catalogs\n");
            }

            // Test 2: List schemas
            System.out.println("=== SCHEMAS (getSchemas) ===");
            try (ResultSet rs = meta.getSchemas()) {
                int count = 0;
                while (rs.next()) {
                    System.out.println("  " + rs.getString("TABLE_SCHEM"));
                    count++;
                }
                System.out.println("Total: " + count + " schemas\n");
            }

            // Test 3: Query information_schema directly
            System.out.println("=== INFORMATION_SCHEMA.SCHEMATA ===");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT SCHEMA_NAME FROM information_schema.schemata")) {
                int count = 0;
                while (rs.next()) {
                    System.out.println("  " + rs.getString(1));
                    count++;
                }
                System.out.println("Total: " + count + " schemas\n");
            }

            // Test 4: Query information_schema.tables
            System.out.println("=== INFORMATION_SCHEMA.TABLES (first 10) ===");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_TYPE FROM information_schema.tables LIMIT 10")) {
                while (rs.next()) {
                    System.out.printf("  %s.%s (%s)%n",
                        rs.getString("TABLE_SCHEMA"),
                        rs.getString("TABLE_NAME"),
                        rs.getString("TABLE_TYPE"));
                }
            }

            // Test 5: getTables with null catalog
            System.out.println("\n=== getTables(null, null, %, TABLE) ===");
            try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                int count = 0;
                while (rs.next() && count < 10) {
                    System.out.printf("  %s.%s%n",
                        rs.getString("TABLE_CAT"),
                        rs.getString("TABLE_NAME"));
                    count++;
                }
                if (count == 10) System.out.println("  ... (showing first 10)");
            }

            // Test 6: List all tables in first user catalog
            if (firstCatalog != null) {
                System.out.println("\n=== TABLES IN CATALOG: " + firstCatalog + " ===");
                try (ResultSet rs = meta.getTables(firstCatalog, null, "%", new String[]{"TABLE", "VIEW"})) {
                    int count = 0;
                    while (rs.next()) {
                        System.out.printf("  %s.%s (%s)%n",
                            rs.getString("TABLE_CAT"),
                            rs.getString("TABLE_NAME"),
                            rs.getString("TABLE_TYPE"));
                        count++;
                    }
                    System.out.println("Total: " + count + " tables/views in " + firstCatalog);
                }

                // Also test with information_schema query (what Dremio uses)
                System.out.println("\n=== INFORMATION_SCHEMA QUERY FOR: " + firstCatalog + " ===");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(
                         "SELECT TABLE_SCHEMA CAT, NULL SCH, TABLE_NAME NME " +
                         "FROM information_schema.tables " +
                         "WHERE TABLE_SCHEMA = '" + firstCatalog + "'")) {
                    int count = 0;
                    while (rs.next()) {
                        System.out.printf("  CAT=%s, SCH=%s, NME=%s%n",
                            rs.getString("CAT"),
                            rs.getString("SCH"),
                            rs.getString("NME"));
                        count++;
                    }
                    System.out.println("Total: " + count + " tables");
                }
            } else {
                System.out.println("\n(No user catalogs found to list tables)");
            }

        } catch (SQLException e) {
            System.err.println("✗ Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
