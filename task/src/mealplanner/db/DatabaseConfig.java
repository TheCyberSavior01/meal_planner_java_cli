package mealplanner.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static String dbUrl = "jdbc:postgresql://localhost:5432/meals_db";
    private static String dbUserName = "postgres";
    private static String dbPass = "yourPass";


    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUsername() {
        return dbUserName;
    }

    public static String getDbPassword() {
        return dbPass;
    }
}
