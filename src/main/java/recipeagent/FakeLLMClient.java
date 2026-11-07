package recipeagent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FakeLLMClient implements LLMClient {
    private static final List<String> NONSENSE = List.of("hockey", "dentist", "purple monkey");
    private static final String NEW_RECIPES = """
            [
            {"name": "Avocado Toast with Egg", "ingredients": [{"name": "Bread", "amount": "2 slices"}, {"name": "Avocado", "amount": "1"}, {"name": "Eggs", "amount": "2"}],
             "instructions": ["Toast the bread.", "Mash the avocado onto the toast.", "Top with fried eggs."], "cookingTime": "10 minutes", "mealType": "Breakfast", "servings": "2", "notes": ""},
            {"name": "Shakshuka", "ingredients": [{"name": "Eggs", "amount": "4"}, {"name": "Diced tomatoes", "amount": "1 can"}, {"name": "Onion", "amount": "1"}, {"name": "Paprika", "amount": "1 tsp"}],
             "instructions": ["Soften the chopped onion.", "Add the tomatoes and paprika and simmer for 10 minutes.", "Crack in the eggs and cover until set."], "cookingTime": "25 minutes", "mealType": "Breakfast", "servings": "2", "notes": ""},
            {"name": "Peanut Butter Banana Toast", "ingredients": [{"name": "Bread", "amount": "2 slices"}, {"name": "Peanut butter", "amount": "2 tbsp"}, {"name": "Banana", "amount": "1"}],
             "instructions": ["Toast the bread.", "Spread with peanut butter and top with sliced banana."], "cookingTime": "5 minutes", "mealType": "Breakfast", "servings": "1", "notes": ""},
            {"name": "Chickpea Salad Sandwich", "ingredients": [{"name": "Chickpeas", "amount": "1 can"}, {"name": "Mayonnaise", "amount": "2 tbsp"}, {"name": "Celery", "amount": "1 stalk"}, {"name": "Bread", "amount": "4 slices"}],
             "instructions": ["Mash the chickpeas.", "Mix in the mayonnaise and chopped celery.", "Spread between the bread."], "cookingTime": "10 minutes", "mealType": "Lunch", "servings": "2", "notes": ""},
            {"name": "Tomato Basil Soup", "ingredients": [{"name": "Diced tomatoes", "amount": "2 cans"}, {"name": "Onion", "amount": "1"}, {"name": "Vegetable broth", "amount": "2 cups"}, {"name": "Basil", "amount": "1/4 cup"}],
             "instructions": ["Soften the chopped onion.", "Add the tomatoes and broth and simmer for 15 minutes.", "Blend with the basil."], "cookingTime": "30 minutes", "mealType": "Lunch", "servings": "4", "notes": ""},
            {"name": "Black Bean Quesadillas", "ingredients": [{"name": "Tortillas", "amount": "4"}, {"name": "Black beans", "amount": "1 can"}, {"name": "Cheddar cheese", "amount": "1 cup"}, {"name": "Salsa", "amount": "1/2 cup"}],
             "instructions": ["Fill the tortillas with beans and cheese and fold them.", "Fry until crisp on both sides.", "Serve with salsa."], "cookingTime": "15 minutes", "mealType": "Lunch", "servings": "2", "notes": ""},
            {"name": "Spicy Peanut Noodles", "ingredients": [{"name": "Noodles", "amount": "200 g"}, {"name": "Peanut butter", "amount": "3 tbsp"}, {"name": "Soy sauce", "amount": "2 tbsp"}, {"name": "Chili flakes", "amount": "1 tsp"}],
             "instructions": ["Cook the noodles.", "Whisk the peanut butter, soy sauce and chili flakes with a splash of hot water.", "Toss the noodles in the sauce."], "cookingTime": "15 minutes", "mealType": "Dinner", "servings": "2", "notes": ""},
            {"name": "Spinach and Egg Fried Rice", "ingredients": [{"name": "Rice", "amount": "2 cups, cooked"}, {"name": "Eggs", "amount": "2"}, {"name": "Spinach", "amount": "2 cups"}, {"name": "Soy sauce", "amount": "2 tbsp"}],
             "instructions": ["Scramble the eggs in a hot pan.", "Add the rice and spinach and fry for 3 minutes.", "Stir in the soy sauce."], "cookingTime": "15 minutes", "mealType": "Dinner", "servings": "2", "notes": ""},
            {"name": "Teriyaki Tofu Bowl", "ingredients": [{"name": "Tofu", "amount": "1 block"}, {"name": "Teriyaki sauce", "amount": "1/4 cup"}, {"name": "Rice", "amount": "1 cup"}, {"name": "Broccoli", "amount": "1 head"}],
             "instructions": ["Cook the rice.", "Fry the cubed tofu until golden, then add the teriyaki sauce.", "Steam the broccoli and serve everything together."], "cookingTime": "30 minutes", "mealType": "Dinner", "servings": "3", "notes": ""}
            ]""";

    private Gson gson = new Gson();
    private List<Recipe> newRecipes = gson.fromJson(NEW_RECIPES, new TypeToken<List<Recipe>>() {});

    @Override
    public String sendMessage(String systemPrompt, List<ChatMessage> messages) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("The AI request was interrupted.");
        }
        String request = messages.get(messages.size() - 1).getContent();
        if (request.toLowerCase().contains("fake error")) {
            throw new RuntimeException("The fake AI failed on purpose, because your message said \"fake error\".");
        }
        JsonObject reply = new JsonObject();
        reply.addProperty("flagged", isNonsense(request));
        List<String> invalidFields = invalidProfileFields(systemPrompt);
        reply.add("invalidProfileFields", gson.toJsonTree(invalidFields));
        String message;
        if (isNonsense(request)) {
            message = "That isn't about cooking, recipes or meal planning, so I can't help with it.";
        } else if (systemPrompt.contains("Task: recommend recipes")) {
            message = recommend(reply, messages);
        } else if (systemPrompt.contains("Task: the user's message is a recipe")) {
            message = parse(reply, request);
        } else if (systemPrompt.contains("Task: help the user change the recipe")) {
            message = modify(reply, systemPrompt, request);
        } else if (systemPrompt.contains("Task: the user's message describes")) {
            message = search(reply, systemPrompt, request);
        } else if (systemPrompt.contains("Task: plan the user's week")) {
            message = plan(reply, systemPrompt, request);
        } else {
            message = shoppingList(reply, systemPrompt);
        }
        if (!invalidFields.isEmpty()) {
            message += " I removed " + String.join(", ", invalidFields) + " from your profile because it isn't a food preference.";
        }
        reply.addProperty("message", "[Fake AI] " + message);
        return reply.toString();
    }

    private String recommend(JsonObject reply, List<ChatMessage> messages) {
        String request = messages.get(messages.size() - 1).getContent();
        if (!request.trim().contains(" ")) {
            reply.add("recipes", new JsonArray());
            return "Could you tell me a bit more, such as which meal it's for or what you'd like to use up?";
        }
        String mealType = "";
        for (ChatMessage message : messages) {
            for (MealTime time : MealTime.values()) {
                if (message.getAuthor().equals("User") && message.getContent().toLowerCase().contains(time.name().toLowerCase())) {
                    mealType = time.name();
                }
            }
        }
        List<Recipe> options = withMealType(newRecipes, mealType);
        Recipe first = options.get(messages.size() % options.size());
        Recipe second = options.get((messages.size() + 1) % options.size());
        reply.add("recipes", gson.toJsonTree(List.of(first, second)));
        return "How about " + first.getName() + " or " + second.getName() + "?";
    }

    private String parse(JsonObject reply, String text) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n")) {
            if (!line.isBlank()) {
                lines.add(line.trim());
            }
        }
        List<Ingredient> ingredients = new ArrayList<>();
        List<String> instructions = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            if (line.startsWith("-")) {
                ingredients.add(new Ingredient(line.substring(1).trim(), ""));
            } else {
                instructions.add(line);
            }
        }
        Recipe recipe = new Recipe(lines.get(0), ingredients, instructions, "", "", "", "", false);
        reply.add("recipe", gson.toJsonTree(recipe));
        return "I organised your recipe \"" + recipe.getName() + "\".";
    }

    private String modify(JsonObject reply, String systemPrompt, String request) {
        Recipe recipe = recipesInPrompt(systemPrompt).get(0);
        if (!request.toLowerCase().contains("apply")) {
            reply.add("recipe", JsonNull.INSTANCE);
            return "To make " + recipe.getName() + " serve twice as many people, double every ingredient."
                    + " A squeeze of lemon at the end would brighten it up too. Say \"apply\" and I'll make both changes.";
        }
        if (recipe.getServings().matches("\\d+")) {
            recipe.setServings(String.valueOf(Integer.parseInt(recipe.getServings()) * 2));
        }
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.setAmount("2 x " + ingredient.getAmount());
        }
        recipe.getInstructions().add("Finish with a squeeze of lemon.");
        reply.add("recipe", gson.toJsonTree(recipe));
        return "Done. I doubled every ingredient and added a lemon step at the end.";
    }

    private String search(JsonObject reply, String systemPrompt, String query) {
        List<String> ids = new ArrayList<>();
        for (Recipe recipe : recipesInPrompt(systemPrompt)) {
            String text = recipe.getName() + " " + recipe.getMealType();
            for (Ingredient ingredient : recipe.getIngredients()) {
                text += " " + ingredient.getName();
            }
            for (String word : query.toLowerCase().split("\\W+")) {
                if (word.length() >= 4 && text.toLowerCase().contains(word) && !ids.contains(recipe.getId())) {
                    ids.add(recipe.getId());
                }
            }
        }
        reply.add("ids", gson.toJsonTree(ids));
        if (ids.isEmpty()) {
            return "None of your saved recipes match that.";
        }
        return "Here are the saved recipes that match.";
    }

    private String plan(JsonObject reply, String systemPrompt, String request) {
        boolean savedOnly = request.contains("Use only my saved recipes.");
        List<Recipe> saved = recipesInPrompt(systemPrompt);
        JsonArray slots = new JsonArray();
        for (DayOfWeek day : DayOfWeek.values()) {
            for (MealTime time : MealTime.values()) {
                List<Recipe> options = withMealType(saved, time.name());
                JsonObject slot = new JsonObject();
                slot.addProperty("day", day.name());
                slot.addProperty("mealTime", time.name());
                if (!savedOnly && (options.isEmpty() || day == DayOfWeek.SUNDAY)) {
                    slot.addProperty("recipeId", "");
                    slot.add("recipe", gson.toJsonTree(withMealType(newRecipes, time.name()).get(0)));
                    slots.add(slot);
                } else if (!options.isEmpty()) {
                    slot.addProperty("recipeId", options.get(day.ordinal() % options.size()).getId());
                    slot.add("recipe", JsonNull.INSTANCE);
                    slots.add(slot);
                }
            }
        }
        reply.add("slots", slots);
        if (slots.isEmpty()) {
            return "You have no saved breakfast, lunch or dinner recipes, so I couldn't plan your week.";
        }
        if (savedOnly) {
            return "Here's your week, using only your saved recipes.";
        }
        return "Here's your week. Some slots have new recipes I wrote, which aren't in your saved recipes.";
    }

    private String shoppingList(JsonObject reply, String systemPrompt) {
        Map<String, Ingredient> needed = new LinkedHashMap<>();
        for (Recipe recipe : recipesInPrompt(systemPrompt)) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                String key = ingredient.getName().toLowerCase();
                if (needed.containsKey(key)) {
                    needed.get(key).setAmount(needed.get(key).getAmount() + " + " + ingredient.getAmount());
                } else if (!systemPrompt.toLowerCase().contains("\n- " + key + ":")) {
                    needed.put(key, ingredient);
                }
            }
        }
        reply.add("ingredients", gson.toJsonTree(new ArrayList<>(needed.values())));
        if (needed.isEmpty()) {
            return "You already have everything you need.";
        }
        String message = "Here's what to buy for your planned meals.";
        if (systemPrompt.contains("Ingredients the user has in their pantry:")) {
            message += " I left out anything that's in your pantry.";
        }
        return message;
    }

    private List<String> invalidProfileFields(String systemPrompt) {
        List<String> fields = new ArrayList<>();
        if (!systemPrompt.contains("Respect it in your answer:")) {
            return fields;
        }
        String profile = systemPrompt.substring(systemPrompt.indexOf("Respect it in your answer:"),
                systemPrompt.indexOf("If any of these settings"));
        for (String line : profile.split("\n")) {
            if (line.startsWith("- ") && isNonsense(line)) {
                fields.add(line.substring(2, line.indexOf(": ")));
            }
        }
        return fields;
    }

    private List<Recipe> recipesInPrompt(String systemPrompt) {
        String start = "Recipes for this task, as JSON:\n";
        String rest = systemPrompt.substring(systemPrompt.indexOf(start) + start.length());
        return gson.fromJson(new JsonReader(new StringReader(rest)), new TypeToken<List<Recipe>>() {});
    }

    private List<Recipe> withMealType(List<Recipe> recipes, String mealType) {
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (mealType.isEmpty() || recipe.getMealType().equalsIgnoreCase(mealType)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    private boolean isNonsense(String text) {
        for (String phrase : NONSENSE) {
            if (text.toLowerCase().contains(phrase)) {
                return true;
            }
        }
        return false;
    }
}
