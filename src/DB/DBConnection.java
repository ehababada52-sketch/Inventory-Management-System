package DB;

import java.sql.*;

public class DBConnection {

    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "warehouse_system";
    private static final String PASS = "w1234";

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                connection = DriverManager.getConnection(URL, USER, PASS);
//                System.out.println(" Connected to Oracle Database successfully!");
            }
        } catch (ClassNotFoundException | SQLException ex) {
//            System.err.println(" Database Connection Failed: " + ex.getMessage());
        }
        return connection;
    }
}
