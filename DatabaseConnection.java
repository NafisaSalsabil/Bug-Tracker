import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConnection {
    // ================= DB CONNECTION =================
    
    private static final String CONFIG_FILE = "db.properties";

    public static Connection getConnection() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Properties properties = new Properties();
        
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            throw new Exception(
                "Could not load database configuration. " +
                "Make sure db.properties exists in the project folder.",
                e
            );
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        if (url == null || user == null || password == null) {
            throw new Exception(
                "Database configuration is incomplete. " +
                "Check db.properties."
            );
            
        }

        Connection conn = DriverManager.getConnection(url, user, password);
        return conn;
    }
}
