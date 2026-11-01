package recipeagent;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class MealPlanManager {
    private static MealPlanManager instance;
    private List<MealSlot> slots = new ArrayList<>();

    private MealPlanManager() {
        for (DayOfWeek day : DayOfWeek.values()) {
            for (MealTime time : MealTime.values()) {
                slots.add(new MealSlot(day, time, null));
            }
        }
    }

    public static MealPlanManager getInstance() {
        if (instance == null) {
            instance = new MealPlanManager();
        }
        return instance;
    }

    public MealSlot getSlot(DayOfWeek day, MealTime time) {
        for (MealSlot slot : slots) {
            if (slot.getDay() == day && slot.getMealTime() == time) {
                return slot;
            }
        }
        return null;
    }

    public boolean assignRecipe(DayOfWeek day, MealTime time, Recipe recipe) {
        MealSlot slot = getSlot(day, time);
        if (slot == null) {
            return false;
        }
        slot.setRecipe(recipe);
        return true;
    }

    public void clearSlot(DayOfWeek day, MealTime time) {
        assignRecipe(day, time, null);
    }

    public void applyPlan(List<MealSlot> generated) {
        for (MealSlot slot : generated) {
            if (slot.getRecipe() != null) {
                assignRecipe(slot.getDay(), slot.getMealTime(), slot.getRecipe());
            }
        }
    }

    public void clearPlan() {
        for (MealSlot slot : slots) {
            slot.setRecipe(null);
        }
    }

    public List<MealSlot> getSlots() {
        return new ArrayList<>(slots);
    }

    public List<Recipe> getPlannedRecipes() {
        List<Recipe> planned = new ArrayList<>();
        for (MealSlot slot : slots) {
            if (slot.getRecipe() != null) {
                planned.add(slot.getRecipe());
            }
        }
        return planned;
    }
}
