package recipeagent;

import java.time.DayOfWeek;

public class MealSlot {
    private DayOfWeek day;
    private MealTime mealTime;
    private Recipe recipe;

    public MealSlot(DayOfWeek day, MealTime mealTime, Recipe recipe) {
        this.day = day;
        this.mealTime = mealTime;
        this.recipe = recipe;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public MealTime getMealTime() {
        return mealTime;
    }

    public void setMealTime(MealTime mealTime) {
        this.mealTime = mealTime;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}
