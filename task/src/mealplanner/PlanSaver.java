package mealplanner;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

public class PlanSaver {
    private Scanner scanner;
    private Connection connection;
    private List<String> ingredientsList = new ArrayList<>();
    private Map<String, Integer> ingredientCounts = new HashMap<>();

    public PlanSaver() {}

    public PlanSaver(Scanner scanner, Connection connection) {
        this.scanner = scanner;
        this.connection = connection;
    }

    public void runSavePlan() {
        fetchAndStore();
        if(checkDB()) {
            writeToFile();
        }else {
            System.out.println("Unable to save. Plan your meals first.");
        }
    }

    private boolean checkDB() {
        String query = "select i.ingredient from ingredients as i join plan as p on i.meal_id = p.meal_id";
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if(resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void writeToFile() {
        try {
            System.out.println("Input a filename: ");
            String fileName = scanner.nextLine();
            PrintWriter writer = new PrintWriter(fileName);
            for (Map.Entry<String, Integer> entry : ingredientCounts.entrySet()) {
                String ingredient = entry.getKey();
                int count = entry.getValue();
                writer.println(ingredient + (count > 1 ? " x" + count : ""));
            }
            writer.close();
            System.out.println("Saved!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchAndStore() {
        String query = "select i.ingredient from ingredients as i join plan as p on i.meal_id = p.meal_id";
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String name = resultSet.getString("ingredient");
                ingredientsList.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (String ingredient : ingredientsList) {
            String[] items = ingredient.split(",");
            for (String item : items) {
                ingredientCounts.put(item, ingredientCounts.getOrDefault(item, 0) + 1);
            }
        }
    }



}
