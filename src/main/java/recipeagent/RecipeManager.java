package recipeagent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager {
    private static RecipeManager instance;
    private Map<String, Recipe> recipes = new LinkedHashMap<>();
    private int nextId = 1;

    private RecipeManager() {
    }

    public static RecipeManager getInstance() {
        if (instance == null) {
            instance = new RecipeManager();
        }
        return instance;
    }

    public boolean saveRecipe(Recipe r) {
        if (r.getName().isBlank() || nameExists(r.getName())) {
            return false;
        }
        r.assignId(String.valueOf(nextId));
        nextId++;
        recipes.put(r.getId(), r);
        return true;
    }

    public Recipe getRecipe(String id) {
        return recipes.get(id);
    }

    public boolean removeRecipe(String id) {
        return recipes.remove(id) != null;
    }

    public boolean updateRecipe(String id, Recipe updatedRecipe) {
        Recipe recipe = recipes.get(id);
        if (recipe == null || updatedRecipe.getName().isBlank()) {
            return false;
        }
        if (!recipe.getName().equalsIgnoreCase(updatedRecipe.getName()) && nameExists(updatedRecipe.getName())) {
            return false;
        }
        recipe.setName(updatedRecipe.getName());
        recipe.setIngredients(updatedRecipe.getIngredients());
        recipe.setInstructions(updatedRecipe.getInstructions());
        recipe.setCookingTime(updatedRecipe.getCookingTime());
        recipe.setMealType(updatedRecipe.getMealType());
        recipe.setServings(updatedRecipe.getServings());
        recipe.setNotes(updatedRecipe.getNotes());
        return true;
    }

    public boolean setFavourite(String id, boolean favourite) {
        Recipe recipe = recipes.get(id);
        if (recipe == null) {
            return false;
        }
        recipe.setFavourite(favourite);
        return true;
    }

    public List<Recipe> getFavourites() {
        List<Recipe> favourites = new ArrayList<>();
        for (Recipe recipe : recipes.values()) {
            if (recipe.isFavourite()) {
                favourites.add(recipe);
            }
        }
        return favourites;
    }

    public boolean nameExists(String name) {
        for (Recipe recipe : recipes.values()) {
            if (recipe.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public List<Recipe> getRecipes() {
        return new ArrayList<>(recipes.values());
    }
}
