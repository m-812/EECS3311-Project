package recipeagent;

public class MealPlanPromptBuilder extends PromptBuilder {
    private static final String TASK_PROMPT = """
            Task: plan the user's week of meals from the criteria in their message, using their profile, pantry and favourites below when they are given. Each day from Monday to Sunday has a breakfast, lunch and dinner slot.
            The app always asks for a plan, so judge flagged by the text after "My criteria:" alone. If that text is nonsense or isn't about food, cooking, diet, nutrition or meal planning, set flagged to true and don't make a plan, even if you could plan the week anyway.
            Add a "slots" field with a list of the slots you fill, each like {"day": "MONDAY", "mealTime": "DINNER", "recipeId": "3", "recipe": null}. day is MONDAY to SUNDAY and mealTime is BREAKFAST, LUNCH or DINNER.
            To use a saved recipe from the list below, put its id in recipeId and set recipe to null. If the user says to use only saved recipes, fill slots this way only. Otherwise, when no saved recipe suits a slot, you may write a new recipe in recipe and set recipeId to "".
            Leave a slot out if nothing suits it. If you can't make a good plan, say what else you need or which requirements to relax in message.""";

    @Override
    public void buildTaskInstructions() {
        prompt.addSection(TASK_PROMPT + "\n" + RECIPE_FORMAT);
    }
}
