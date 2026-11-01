package projet;

import java.sql.*;

public class connexion {

    //Paramètres de connexion 
    private static final String URL= "jdbc:oracle:thin:@localhost:1521/XEPDB1";
    private static final String USER= "agenda_user";
    private static final String PASSWORD= "agenda123";

    private static Connection instance = null;

    private connexion() {}

    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver Oracle introuvable.");
            }
        }
        return instance;
    }
}