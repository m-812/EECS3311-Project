package recipeagent;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class ResponseParser {
    public AIResult<List<Recipe>> parseRecipes(String json) {
        JsonObject reply = JsonParser.parseString(extractJson(json)).getAsJsonObject();
        String message = reply.get("message").getAsString();
        if (reply.get("flagged").getAsBoolean()) {
            return AIResult.flagged(message);
        }
        List<Recipe> recipes = new Gson().fromJson(reply.get("recipes"), new TypeToken<List<Recipe>>() {});
        return AIResult.success(recipes, message);
    }

    public AIResult<Recipe> parseRecipe(String json) {
        JsonObject reply = JsonParser.parseString(extractJson(json)).getAsJsonObject();
        String message = reply.get("message").getAsString();
        if (reply.get("flagged").getAsBoolean()) {
            return AIResult.flagged(message);
        }
        Recipe recipe = new Gson().fromJson(reply.get("recipe"), Recipe.class);
        return AIResult.success(recipe, message);
    }

    public AIResult<List<String>> parseIds(String json) {
        JsonObject reply = JsonParser.parseString(extractJson(json)).getAsJsonObject();
        String message = reply.get("message").getAsString();
        if (reply.get("flagged").getAsBoolean()) {
            return AIResult.flagged(message);
        }
        List<String> ids = new Gson().fromJson(reply.get("ids"), new TypeToken<List<String>>() {});
        return AIResult.success(ids, message);
    }

    public AIResult<List<MealSlot>> parseMealPlan(String json, List<Recipe> savedRecipes) {
        JsonObject reply = JsonParser.parseString(extractJson(json)).getAsJsonObject();
        String message = reply.get("message").getAsString();
        if (reply.get("flagged").getAsBoolean()) {
            return AIResult.flagged(message);
        }
        List<MealSlot> slots = new ArrayList<>();
        for (JsonElement element : reply.getAsJsonArray("slots")) {
            JsonObject slot = element.getAsJsonObject();
            Recipe recipe = new Gson().fromJson(slot.get("recipe"), Recipe.class);
            for (Recipe saved : savedRecipes) {
                if (saved.getId().equals(slot.get("recipeId").getAsString())) {
                    recipe = saved;
                }
            }
            if (recipe != null) {
                DayOfWeek day = DayOfWeek.valueOf(slot.get("day").getAsString().toUpperCase());
                MealTime time = MealTime.valueOf(slot.get("mealTime").getAsString().toUpperCase());
                slots.add(new MealSlot(day, time, recipe));
            }
        }
        return AIResult.success(slots, message);
    }

    public AIResult<List<Ingredient>> parseIngredients(String json) {
        JsonObject reply = JsonParser.parseString(extractJson(json)).getAsJsonObject();
        String message = reply.get("message").getAsString();
        if (reply.get("flagged").getAsBoolean()) {
            return AIResult.flagged(message);
        }
        List<Ingredient> ingredients = new Gson().fromJson(reply.get("ingredients"),
                new TypeToken<List<Ingredient>>() {});
        return AIResult.success(ingredients, message);
    }

    public List<String> parseInvalidProfileFields(String json) {
        JsonObject reply = JsonParser.parseString(extractJson(json)).getAsJsonObject();
        return new Gson().fromJson(reply.get("invalidProfileFields"), new TypeToken<List<String>>() {});
    }

    private String extractJson(String raw) {
        String found = null;
        for (int i = 0; i < raw.length(); i++) {
            if (raw.charAt(i) == '{') {
                try {
                    JsonElement element = JsonParser.parseReader(new JsonReader(new StringReader(raw.substring(i))));
                    if (element.isJsonObject() && element.getAsJsonObject().has("flagged")) {
                        found = element.toString();
                    }
                } catch (JsonParseException e) {
                }
            }
        }
        if (found == null) {
            throw new RuntimeException("The AI's reply couldn't be read.");
        }
        return found;
    }
}
