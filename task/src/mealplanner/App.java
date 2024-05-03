package mealplanner;


import mealplanner.db.DB;


import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class App {
    private Connection connection;
    private Set<Meal> meals = new HashSet<>();
    private Scanner scanner = new Scanner(System.in);
    private boolean appRunning = true;

    public App() {
    }

    private Set<Meal> getMeals() {
        return meals;
    }

    private void setMeals(Set<Meal> meals) {
        this.meals = meals;
    }

    public void run() {

        connectToDatabase();
        createTables();
        showMainMenu();

        while (appRunning) {
            String option = scanner.nextLine();
            switch (option) {
                case "add":
                    addMeals();
                    break;
                case "show":
                    showMeals();
                    showMainMenu();
                    break;
                case "plan":
                    Plan plan = new Plan(scanner, connection);
                    plan.executePlan();
                    showMainMenu();
                    break;
                case "save":
                    PlanSaver planSaver = new PlanSaver(scanner, connection);
                    planSaver.runSavePlan();
                    showMainMenu();
                    break;
                case "exit":
                    System.out.println("Bye!");
                    appRunning = false;
                    break;
                default:
                    showMainMenu();
            }
        }
    }

    private void createTables() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("create table if not exists meals (" +
                    "category varchar," +
                    "meal varchar," +
                    "meal_id int primary key" +
                    ")");
            statement.executeUpdate("CREATE TABLE if not exists ingredients(\n" +
                    "   ingredient_id INT PRIMARY KEY ,\n" +
                    "   ingredient VARCHAR(255),\n" +
                    "   meal_id INT,\n" +
                    "   CONSTRAINT fk_meal\n" +
                    "      FOREIGN KEY(meal_id)\n" +
                    "        REFERENCES meals(meal_id)\n" +
                    ")");
            //statement.executeUpdate("DROP TABLE IF EXISTS plan");
            statement.executeUpdate("CREATE TABLE if not exists plan(\n" +
                    "   meal_option VARCHAR ,\n" +
                    "   category VARCHAR(255),\n" +
                    "   meal_id INT,\n" +
                    "   CONSTRAINT fk_meal\n" +
                    "      FOREIGN KEY(meal_id)\n" +
                    "        REFERENCES meals(meal_id)\n" +
                    ")");

        } catch(SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
    }

    private void connectToDatabase() {
        try {
            connection = DB.connect();
            assert connection != null;
            connection.setAutoCommit(true);

            Statement statement = connection.createStatement();


        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void showMeals() {
        boolean showCategoryInputRunner = true;

        System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");

        while (showCategoryInputRunner == true) {
            String showCategoryInput = scanner.nextLine();
            switch (showCategoryInput) {
                case "breakfast":
                    displayMeals("breakfast");
                    showCategoryInputRunner = false;
                    break;
                case "lunch":
                    displayMeals("lunch");
                    showCategoryInputRunner = false;
                    break;
                case "dinner":
                    displayMeals("dinner");
                    showCategoryInputRunner = false;
                    break;
                default:
                    System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            }

        }
    }

    private void displayMeals(String category) {
        try {
            String query = "select m.category, m.meal, i.ingredient from meals as m, ingredients as i where (m.meal_id = i.meal_id)" +
                    "and (m.category = ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, category);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                System.out.println("No meals found.");
            }else {
                System.out.println("Category: " + resultSet.getString("category"));
                do {
                    String ingredients = resultSet.getString("ingredient");
                    List<String> ingredientsList = List.of(ingredients.split(","));
                    System.out.println();
                    System.out.println("Name: " + resultSet.getString("meal"));
                    System.out.println("Ingredients: ");
                    for (String ingredient : ingredientsList) {
                        System.out.println(ingredient.trim());
                    }
                } while (resultSet.next());
                System.out.println();
            }
            preparedStatement.close();
        } catch (SQLException e) {
            //System.err.println(e.getMessage());
        }
    }

    private void addMeals() {
        List<String> mealCategory = List.of("breakfast", "lunch", "dinner");
        Meal meal = new Meal();
        boolean inputCategoryRunner = true;

        System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");

        while(inputCategoryRunner) {
            String inputCategory = scanner.nextLine();
            if (mealCategory.contains(inputCategory)) {
                meal.setCategory(inputCategory);
                boolean inputNameRunner = true;
                System.out.println("Input the meal's name: ");
                while(inputNameRunner) {
                    String inputName = scanner.nextLine();
                    if (inputValidator(inputName)) {
                        meal.setName(inputName);
                        boolean inputIngredientsRunner = true;
                        System.out.println("Input the ingredients: ");
                        while (inputIngredientsRunner) {
                            String inputIngredient = scanner.nextLine();
                            if (ingredientsValidator(inputIngredient)) {
                                List<String> ingredientsList = List.of(inputIngredient.split(", "));
                                meal.setIngredients(ingredientsList);
                                meals.add(meal);

                                try {
                                    Statement statement = connection.createStatement();
                                    int idCounter = 0;
                                    String idCounterQuery = "select * from meals";
                                    ResultSet rs = statement.executeQuery(idCounterQuery);
                                    while (rs.next()) {
                                        idCounter++;
                                    }
                                    statement.close();
                                    // write meal to the meal table
                                    String insertMealQuery = "insert into meals (category, meal, meal_id) values (?, ?, ?)";
                                    PreparedStatement preparedMealStatement = connection.prepareStatement(insertMealQuery);
                                    preparedMealStatement.setString(1, inputCategory);
                                    preparedMealStatement.setString(2, inputName);
                                    preparedMealStatement.setInt(3, idCounter);
                                    preparedMealStatement.executeUpdate();
                                    preparedMealStatement.close();
                                    // write ingredient to the ingredients table
                                    String insertIngredientsQuery = "insert into ingredients (ingredient_id, ingredient, meal_id) values (?, ?, ?)";
                                    PreparedStatement preparedIngredientsStatement = connection.prepareStatement(insertIngredientsQuery);
                                    preparedIngredientsStatement.setInt(1, idCounter);
                                    preparedIngredientsStatement.setString(2, inputIngredient);
                                    preparedIngredientsStatement.setInt(3, idCounter);
                                    preparedIngredientsStatement.executeUpdate();
                                    preparedIngredientsStatement.close();
                                    System.out.println("The meal has been added!");
                                    showMainMenu();
                                    inputIngredientsRunner = false;
                                } catch (SQLException e) {
                                    //System.err.println(e.getMessage());
                                }
                            }else {
                                System.out.println("Wrong format. Use letters only!");
                            }
                        inputNameRunner = false;
                        }
                    } else {
                        System.out.println("Wrong format. Use letters only!");
                    }
                inputCategoryRunner = false;
                }
            }else {
                System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            }
        }
    }

    private boolean inputValidator(String inputString) {
        inputString = inputString.trim();
        String regex = "^[\\p{L}]+(?:\\s[\\p{L}]+)*(?:,\\s*[\\p{L}]+)*$";
        if (inputString.isEmpty() || !inputString.matches(regex)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean ingredientsValidator(String inputString) {
        String regexIngredients = "^(?!\\s*$)([\\p{L}\\s]+)$";
        String[] ingredients = inputString.split(",");

        for (String ingredient : ingredients) {
            if (!ingredient.trim().matches(regexIngredients)) {
                return false;
            }
        }

        return true;
    }

    private void showMainMenu() {
        System.out.println("What would you like to do (add, show, plan, save, exit)?");
    }

}
