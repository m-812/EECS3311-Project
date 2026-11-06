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
        // TODO: needs AIAgent.recommendRecipes
        return null;
    }

    public boolean saveRecommendedRecipe(Recipe r) {
        return recipeManager.saveRecipe(r);
    }

    public AIResult<Recipe> addPersonalRecipe(String text) {
        // TODO: needs AIAgent.parsePersonalRecipe
        return null;
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
        // TODO: needs AIAgent.requestModification
        return null;
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
        // TODO: needs AIAgent.searchSavedRecipes
        return null;
    }

    public List<Recipe> clearSearch() {
        return firstPage();
    }

    public boolean setFavourite(String id, boolean fav) {
        return recipeManager.setFavourite(id, fav);
    }
}
