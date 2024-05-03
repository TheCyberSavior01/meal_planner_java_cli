package mealplanner;

import java.util.HashMap;
import java.util.List;

public interface PlanInterface {
    void executePlan();
    void fetchMeals(String category, HashMap<Integer, String> mealMap);
}
