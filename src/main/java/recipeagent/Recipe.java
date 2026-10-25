package recipeagent;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private String id;
    private String name;
    private List<Ingredient> ingredients;
    private List<String> instructions;
    private String cookingTime;
    private String mealType;
    private String servings;
    private String notes;
    private boolean favourite = false;

    public Recipe(String name, List<Ingredient> ingredients, List<String> instructions, String cookingTime,
            String mealType, String servings, String notes, boolean favourite) {
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.cookingTime = cookingTime;
        this.mealType = mealType;
        this.servings = servings;
        this.notes = notes;
        this.favourite = favourite;
    }

    public Recipe(Recipe other) {
        this.id = other.id;
        this.name = other.name;
        this.ingredients = new ArrayList<>();
        for (Ingredient ingredient : other.ingredients) {
            this.ingredients.add(new Ingredient(ingredient));
        }
        this.instructions = new ArrayList<>(other.instructions);
        this.cookingTime = other.cookingTime;
        this.mealType = other.mealType;
        this.servings = other.servings;
        this.notes = other.notes;
        this.favourite = other.favourite;
    }

    void assignId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public String getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(String cookingTime) {
        this.cookingTime = cookingTime;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getServings() {
        return servings;
    }

    public void setServings(String servings) {
        this.servings = servings;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }
}
