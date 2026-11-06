package recipeagent;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;

public abstract class PromptBuilder {
    protected static final String BASE_RULES = """
            You are the AI agent in a recipe and meal planning app. You only help with cooking, recipes, ingredients, nutrition and meal planning.
            Reply with one JSON object and nothing else, with no markdown code fences. Always include these fields:
            "flagged": true if the user's request is nonsensical or not about cooking, recipes or meal planning, otherwise false.
            "message": a short reply to show the user. If flagged is true, explain why the request can't be handled.
            "invalidProfileFields": a list of profile setting names, explained in the profile section. Use [] if there is no profile section.
            The task below adds one more field. If flagged is true, set that field to null.""";

    protected static final String RECIPE_FORMAT = """
            Write every recipe as a JSON object in this format:
            {"name": "Recipe name", "ingredients": [{"name": "Ingredient name", "amount": "How much"}], "instructions": ["First step", "Second step"], "cookingTime": "30 minutes", "mealType": "Dinner", "servings": "4", "notes": "Any extra notes"}
            Use "" for any text you don't know and [] for an empty list.""";

    protected Prompt prompt;

    public void createNewPrompt() {
        prompt = new Prompt();
        prompt.addSection(BASE_RULES);
    }

    public Prompt getPrompt() {
        return prompt;
    }

    public abstract void buildTaskInstructions();

    public void buildProfile(Map<String, String> profile) {
        String settings = "";
        for (Map.Entry<String, String> setting : profile.entrySet()) {
            if (!setting.getValue().isBlank()) {
                settings += "\n- " + setting.getKey() + ": " + setting.getValue();
            }
        }
        if (!settings.isEmpty()) {
            prompt.addSection("The user's preference profile. Respect it in your answer:" + settings
                    + "\nIf any of these settings is nonsense or not a food preference, ignore it, add its exact name"
                    + " (the text before the colon) to invalidProfileFields, and tell the user in message that it was"
                    + " removed from their profile.");
        }
    }

    public void buildPantry(List<PantryItem> items) {
        String pantry = "Ingredients the user has in their pantry:";
        for (PantryItem item : items) {
            pantry += "\n- " + item.getName() + ": " + item.getQuantity();
        }
        prompt.addSection(pantry);
    }

    public void buildFavourites(List<Recipe> recipes) {
        String favourites = "The user's favourite recipes. Lean towards recipes like these:";
        for (Recipe recipe : recipes) {
            favourites += "\n- " + recipe.getName();
        }
        prompt.addSection(favourites);
    }

    public void buildRecipes(List<Recipe> recipes) {
        prompt.addSection("Recipes for this task, as JSON:\n" + new Gson().toJson(recipes));
    }
}
