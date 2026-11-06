package recipeagent;

import java.time.DayOfWeek;
import java.util.List;

public class MealPlanController {
    private MealPlanManager mealPlanManager;
    private RecipeManager recipeManager;
    private AIAgent aiAgent;

    public MealPlanController(AIAgent agent) {
        mealPlanManager = MealPlanManager.getInstance();
        recipeManager = RecipeManager.getInstance();
        aiAgent = agent;
    }

    public List<MealSlot> getSlots() {
        return mealPlanManager.getSlots();
    }

    public boolean assignRecipe(DayOfWeek day, MealTime time, String recipeId) {
        Recipe recipe = recipeManager.getRecipe(recipeId);
        if (recipe == null) {
            return false;
        }
        return mealPlanManager.assignRecipe(day, time, recipe);
    }

    public void clearSlot(DayOfWeek day, MealTime time) {
        mealPlanManager.clearSlot(day, time);
    }

    public void clearPlan() {
        mealPlanManager.clearPlan();
    }

    public AIResult<List<MealSlot>> generateMealPlan(String criteria, boolean savedOnly) {
        // TODO: needs AIAgent.generateMealPlan
        return null;
    }

    public AIResult<List<Ingredient>> generateShoppingList(boolean usePantry) {
        // TODO: needs AIAgent.generateShoppingList
        return null;
    }
}
