package recipeagent;

import java.util.ArrayList;
import java.util.List;

public class RecipeController {
    private RecipeManager recipeManager;
    private AIAgent aiAgent;
    private List<ChatMessage> chatHistory;
    private Recipe editDraft;
    private RecipePageIterator pageIterator;
    public static final int PAGE_SIZE = 10;

    public RecipeController(AIAgent agent) {
        recipeManager = RecipeManager.getInstance();
        aiAgent = agent;
        chatHistory = new ArrayList<>();
    }

    public AIResult<List<Recipe>> requestRecommendations(String request) {
        chatHistory.add(new ChatMessage("User", request));
        AIResult<List<Recipe>> result = aiAgent.recommendRecipes(request, chatHistory);
        if (result.isSuccess() || result.isFlagged()) {
            chatHistory.add(new ChatMessage("AI", result.getMessage()));
        }
        return result;
    }

    public List<ChatMessage> getChatHistory() {
        return new ArrayList<>(chatHistory);
    }

    public boolean saveRecommendedRecipe(Recipe r) {
        return recipeManager.saveRecipe(r);
    }

    public AIResult<Recipe> addPersonalRecipe(String text) {
        AIResult<Recipe> result = aiAgent.parsePersonalRecipe(text);
        if (result.isSuccess() && !recipeManager.saveRecipe(result.getData())) {
            return AIResult.error("The recipe couldn't be saved because its name is blank or already taken.");
        }
        return result;
    }

    public List<Recipe> firstPage() {
        pageIterator = new RecipePageIterator(recipeManager.getRecipes(), PAGE_SIZE);
        return pageIterator.next();
    }

    public List<Recipe> nextPage() {
        return pageIterator.next();
    }

    public List<Recipe> previousPage() {
        return pageIterator.previous();
    }

    public boolean hasNextPage() {
        return pageIterator.hasNext();
    }

    public boolean hasPreviousPage() {
        return pageIterator.hasPrevious();
    }

    public boolean removeRecipe(String id) {
        return recipeManager.removeRecipe(id);
    }

    public Recipe startEditing(String id) {
        editDraft = new Recipe(recipeManager.getRecipe(id));
        chatHistory.clear();
        return editDraft;
    }

    public AIResult<Recipe> requestModification(String request) {
        chatHistory.add(new ChatMessage("User", request));
        AIResult<Recipe> result = aiAgent.requestModification(editDraft, request, chatHistory);
        if (result.isSuccess() || result.isFlagged()) {
            chatHistory.add(new ChatMessage("AI", result.getMessage()));
        }
        if (result.isSuccess() && result.getData() != null) {
            Recipe recipe = result.getData();
            editDraft.setName(recipe.getName());
            editDraft.setIngredients(recipe.getIngredients());
            editDraft.setInstructions(recipe.getInstructions());
            editDraft.setCookingTime(recipe.getCookingTime());
            editDraft.setMealType(recipe.getMealType());
            editDraft.setServings(recipe.getServings());
            editDraft.setNotes(recipe.getNotes());
        }
        return result;
    }

    public boolean saveEdit() {
        boolean saved = recipeManager.updateRecipe(editDraft.getId(), editDraft);
        if (saved) {
            editDraft = null;
            chatHistory.clear();
        }
        return saved;
    }

    public void cancelEdit() {
        editDraft = null;
        chatHistory.clear();
    }

    public AIResult<List<Recipe>> searchRecipes(String query) {
        AIResult<List<Recipe>> result = aiAgent.searchSavedRecipes(query);
        if (!result.isSuccess()) {
            return result;
        }
        pageIterator = new RecipePageIterator(result.getData(), PAGE_SIZE);
        return AIResult.success(pageIterator.next(), result.getMessage());
    }

    public List<Recipe> clearSearch() {
        return firstPage();
    }

    public boolean setFavourite(String id, boolean fav) {
        return recipeManager.setFavourite(id, fav);
    }
}
