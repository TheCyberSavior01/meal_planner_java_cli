package mealplanner;

import java.sql.*;
import java.util.*;

public class Plan implements PlanInterface {
    private Scanner scanner;
    private Connection connection;
    private List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private HashMap<String, HashMap<Integer, String>> chosenMeals = new HashMap<>();
    private List<HashMap<String, String>> chosenmealsList = new ArrayList<>();
    private HashMap<Integer, String> breakfastMap = new HashMap<>();
    private HashMap<Integer, String> luncheMap = new HashMap<>();
    private HashMap<Integer, String> dinnerMap = new HashMap<>();
    private List<String> breakfastList = new ArrayList<>();
    private List<String> lunchList = new ArrayList<>();
    private List<String> dinnerList = new ArrayList<>();

    public Plan(Scanner scanner, Connection connection) {
        this.scanner = scanner;
        this.connection = connection;
    }

    @Override
    public void executePlan() {
        fetchMeals("breakfast", breakfastMap);
        fetchMeals("lunch", luncheMap);
        fetchMeals("dinner", dinnerMap);

        breakfastList.addAll(breakfastMap.values());
        lunchList.addAll(luncheMap.values());
        dinnerList.addAll(dinnerMap.values());
        Collections.sort(breakfastList);
        Collections.sort(lunchList);
        Collections.sort(dinnerList);

        for (String day : days) {
            System.out.println(day);

            // Choose breakfast
            assignMeal("breakfast", breakfastList, day, breakfastMap);

            // Choose lunch
            assignMeal("lunch", lunchList, day, luncheMap);

            // Choose dinner
            assignMeal("dinner", dinnerList, day, dinnerMap);

            System.out.println("Yeah! We planned the meals for " + day + ".");
            System.out.println();
        }

        writeToDatabase();
        showPlan();

    }

    private void writeToDatabase() {
       for (HashMap<String, String> meal : chosenmealsList) {
           try{
               String mealOption = meal.get("day");
               String category = meal.get("category");
               int mealId = Integer.parseInt(meal.get("id"));

               String insertPlanQuery = "insert into plan (meal_option, category, meal_id) values (?, ?, ?)";
               PreparedStatement preparedMealStatement = connection.prepareStatement(insertPlanQuery);
               preparedMealStatement.setString(1, mealOption);
               preparedMealStatement.setString(2, category);
               preparedMealStatement.setInt(3, mealId);
               preparedMealStatement.executeUpdate();
               preparedMealStatement.close();
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }
    }

    private void assignMeal(String category, List<String> mealList, String day, HashMap<Integer, String> mealMap) {
        //System.out.println(category + ":");
        for (String meal : mealList) {
            System.out.println(meal);
        }
        System.out.println("Choose the " + category + " for " + day + " from the list above:");
        boolean inputRunner = true;
        while (inputRunner) {
            String mealnput = scanner.nextLine();
            if (mealList.contains(mealnput)) {
              HashMap<String, String> map = new HashMap<>();
              map.put("day", day);
              map.put("meal", mealnput);
              map.put("category", category);
              for (Map.Entry<Integer, String> entry : mealMap.entrySet()) {
                  if (Objects.equals(entry.getValue(), mealnput)) {
                      map.put("id", entry.getKey().toString());
                      inputRunner = false;
                      break;
                  }
              }
              chosenmealsList.add(map);
            } else {
                System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
            }
        }
    }

    private void showPlan() {
        List<String> categories = List.of("breakfast", "lunch", "dinner");
        for (String day : days) {
            System.out.println(day);
            for (String category: categories) {
                for(HashMap<String, String> meal: chosenmealsList) {
                    String getDay = meal.get("day");
                    String getCategory = meal.get("category");
                    String getName = meal.get("meal");
                    if(day.equals(getDay) && category.equals(getCategory)) {
                        System.out.println(category + ": " + getName);
                    }
                }
            }
            System.out.println();

        }
    }


    @Override
    public void fetchMeals(String category, HashMap<Integer, String> mealMap) {
        String query = "select * from meals where category = ?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, category);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("meal");
                int id = resultSet.getInt("meal_id");
                mealMap.put(id, name.trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
