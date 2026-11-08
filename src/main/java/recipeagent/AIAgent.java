package recipeagent;

import java.util.ArrayList;
import java.util.List;

public class AIAgent {
    private LLMClient client;
    private ResponseParser parser;
    private RecipeManager recipeManager;
    private PantryManager pantryManager;
    private ProfileManager profileManager;
    private MealPlanManager mealPlanManager;

    public AIAgent(LLMClient client) {
        this.client = client;
        parser = new ResponseParser();
        recipeManager = RecipeManager.getInstance();
        pantryManager = PantryManager.getInstance();
        profileManager = ProfileManager.getInstance();
        mealPlanManager = MealPlanManager.getInstance();
    }

    public AIResult<List<Recipe>> recommendRecipes(String request, List<ChatMessage> history) {
        try {
            PromptBuilder builder = new RecommendedRecipePromptBuilder();
            builder.createNewPrompt();
            builder.buildTaskInstructions();
            builder.buildProfile(profileManager.getSettings());
            builder.buildPantry(pantryManager.getItems());
            builder.buildFavourites(recipeManager.getFavourites());
            String reply = client.sendMessage(builder.getPrompt().toText(), history);
            for (String name : parser.parseInvalidProfileFields(reply)) {
                profileManager.clearSetting(name);
            }
            return parser.parseRecipes(reply);
        } catch (RuntimeException e) {
            return AIResult.error(e.getMessage());
        }
    }

    public AIResult<Recipe> parsePersonalRecipe(String text) {
        try {
            PromptBuilder builder = new ParseRecipePromptBuilder();
            builder.createNewPrompt();
            builder.buildTaskInstructions();
            String reply = client.sendMessage(builder.getPrompt().toText(), List.of(new ChatMessage("User", text)));
            return parser.parseRecipe(reply);
        } catch (RuntimeException e) {
            return AIResult.error(e.getMessage());
        }
    }

    public AIResult<Recipe> requestModification(Recipe recipe, String request, List<ChatMessage> history) {
        try {
            PromptBuilder builder = new ModifyRecipePromptBuilder();
            builder.createNewPrompt();
            builder.buildTaskInstructions();
            builder.buildProfile(profileManager.getSettings());
            builder.buildPantry(pantryManager.getItems());
            builder.buildRecipes(List.of(recipe));
            String reply = client.sendMessage(builder.getPrompt().toText(), history);
            for (String name : parser.parseInvalidProfileFields(reply)) {
                profileManager.clearSetting(name);
            }
            return parser.parseRecipe(reply);
        } catch (RuntimeException e) {
            return AIResult.error(e.getMessage());
        }
    }

    public AIResult<List<Recipe>> searchSavedRecipes(String query) {
        try {
            PromptBuilder builder = new SearchRecipePromptBuilder();
            builder.createNewPrompt();
            builder.buildTaskInstructions();
            builder.buildRecipes(recipeManager.getRecipes());
            String reply = client.sendMessage(builder.getPrompt().toText(), List.of(new ChatMessage("User", query)));
            AIResult<List<String>> ids = parser.parseIds(reply);
            if (ids.isFlagged()) {
                return AIResult.flagged(ids.getMessage());
            }
            List<Recipe> matches = new ArrayList<>();
            for (String id : ids.getData()) {
                Recipe match = recipeManager.getRecipe(id);
                if (match != null) {
                    matches.add(match);
                }
            }
            return AIResult.success(matches, ids.getMessage());
        } catch (RuntimeException e) {
            return AIResult.error(e.getMessage());
        }
    }

    public AIResult<List<MealSlot>> generateMealPlan(String criteria, boolean savedOnly) {
        try {
            List<Recipe> savedRecipes = recipeManager.getRecipes();
            PromptBuilder builder = new MealPlanPromptBuilder();
            builder.createNewPrompt();
            builder.buildTaskInstructions();
            builder.buildProfile(profileManager.getSettings());
            builder.buildPantry(pantryManager.getItems());
            builder.buildFavourites(recipeManager.getFavourites());
            builder.buildRecipes(savedRecipes);
            String request = "Plan my meals for the week.";
            if (!criteria.isBlank()) {
                request += "\nMy criteria: " + criteria;
            }
            if (savedOnly) {
                request += "\nUse only my saved recipes.";
            }
            String reply = client.sendMessage(builder.getPrompt().toText(), List.of(new ChatMessage("User", request)));
            for (String name : parser.parseInvalidProfileFields(reply)) {
                profileManager.clearSetting(name);
            }
            AIResult<List<MealSlot>> result = parser.parseMealPlan(reply, savedRecipes);
            if (savedOnly && result.isSuccess()) {
                result.getData().removeIf(slot -> !savedRecipes.contains(slot.getRecipe()));
            }
            return result;
        } catch (RuntimeException e) {
            return AIResult.error(e.getMessage());
        }
    }

    public AIResult<List<Ingredient>> generateShoppingList(boolean usePantry) {
        try {
            PromptBuilder builder = new ShoppingListPromptBuilder();
            builder.createNewPrompt();
            builder.buildTaskInstructions();
            builder.buildRecipes(mealPlanManager.getPlannedRecipes());
            if (usePantry) {
                builder.buildPantry(pantryManager.getItems());
            }
            String reply = client.sendMessage(builder.getPrompt().toText(),
                    List.of(new ChatMessage("User", "Make my shopping list.")));
            return parser.parseIngredients(reply);
        } catch (RuntimeException e) {
            return AIResult.error(e.getMessage());
        }
    }
}
