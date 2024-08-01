package company.appxeiva;

import java.sql.Connection;
import java.sql.DriverManager;

public class database {

    public static Connection connectDb() {
        try {
            Class.forName("org.sqlite.JDBC");
            // Usar una ruta relativa
            String url = "jdbc:sqlite:bd_appXeiva.sqlite";
            Connection connect = DriverManager.getConnection(url);
            return connect;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
