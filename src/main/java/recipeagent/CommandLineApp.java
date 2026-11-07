package recipeagent;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class CommandLineApp {
    private RecipeController recipeController;
    private MealPlanController mealPlanController;
    private PantryController pantryController;
    private ProfileController profileController;
    private Scanner scanner = new Scanner(System.in);

    public CommandLineApp(AIAgent agent) {
        recipeController = new RecipeController(agent);
        mealPlanController = new MealPlanController(agent);
        pantryController = new PantryController();
        profileController = new ProfileController();
    }

    public void run() {
        while (true) {
            System.out.println();
            System.out.println("=== AI Recipe & Meal Planner ===");
            System.out.println("1. My recipes (view, search, favourite, edit, remove)");
            System.out.println("2. Add a personal recipe");
            System.out.println("3. Get recipe recommendations");
            System.out.println("4. Meal plan and shopping list");
            System.out.println("5. Pantry");
            System.out.println("6. Profile");
            System.out.println("0. Quit");
            switch (ask("Choose: ")) {
                case "1" -> recipesMenu();
                case "2" -> addPersonalRecipe();
                case "3" -> recommendMenu();
                case "4" -> mealPlanMenu();
                case "5" -> pantryMenu();
                case "6" -> profileMenu();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Please type one of the numbers above.");
            }
        }
    }

    private void recipesMenu() {
        Recipe recipe = browseRecipes("open a recipe");
        while (recipe != null) {
            recipeMenu(recipe);
            recipe = browseRecipes("open a recipe");
        }
    }

    private Recipe browseRecipes(String chooseText) {
        List<Recipe> page = recipeController.firstPage();
        while (true) {
            System.out.println();
            if (page.isEmpty()) {
                System.out.println("(no recipes)");
            }
            for (int i = 0; i < page.size(); i++) {
                Recipe recipe = page.get(i);
                System.out.println((i + 1) + ". " + (recipe.isFavourite() ? "★ " : "") + recipe.getName());
            }
            String options = "Type a number to " + chooseText;
            if (recipeController.hasNextPage()) {
                options += ", n = next page";
            }
            if (recipeController.hasPreviousPage()) {
                options += ", p = previous page";
            }
            System.out.println(options + ", s = search, c = clear search, b = back");
            String choice = ask("Choose: ").toLowerCase();
            if (choice.equals("b")) {
                return null;
            } else if (choice.equals("n") && recipeController.hasNextPage()) {
                page = recipeController.nextPage();
            } else if (choice.equals("p") && recipeController.hasPreviousPage()) {
                page = recipeController.previousPage();
            } else if (choice.equals("s")) {
                String query = ask("Describe the recipes you're looking for: ");
                if (!query.isEmpty()) {
                    System.out.println("Thinking…");
                    AIResult<List<Recipe>> result = recipeController.searchRecipes(query);
                    if (printResult(result)) {
                        page = result.getData();
                    }
                }
            } else if (choice.equals("c")) {
                page = recipeController.clearSearch();
            } else {
                int number = toNumber(choice);
                if (number >= 1 && number <= page.size()) {
                    return page.get(number - 1);
                }
                System.out.println("Please choose one of the options above.");
            }
        }
    }

    private void recipeMenu(Recipe recipe) {
        while (true) {
            System.out.println();
            System.out.println(formatRecipe(recipe));
            System.out.println();
            System.out.println("1. " + (recipe.isFavourite() ? "Unfavourite" : "Favourite"));
            System.out.println("2. Edit (with AI help)");
            System.out.println("3. Remove");
            System.out.println("0. Back");
            switch (ask("Choose: ")) {
                case "1" -> {
                    if (!recipeController.setFavourite(recipe.getId(), !recipe.isFavourite())) {
                        System.out.println("That recipe no longer exists.");
                        return;
                    }
                }
                case "2" -> editRecipe(recipe.getId());
                case "3" -> {
                    if (ask("Remove \"" + recipe.getName() + "\"? (y/n): ").equalsIgnoreCase("y")) {
                        System.out.println(recipeController.removeRecipe(recipe.getId()) ? "Removed."
                                : "That recipe no longer exists.");
                        return;
                    }
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("Please type one of the numbers above.");
            }
        }
    }

    private void editRecipe(String id) {
        Recipe draft = recipeController.startEditing(id);
        while (true) {
            System.out.println();
            System.out.println("Editing \"" + draft.getName() + "\". Changes are only kept if you save.");
            System.out.println("1. Name: " + draft.getName());
            System.out.println("2. Meal type: " + draft.getMealType());
            System.out.println("3. Cooking time: " + draft.getCookingTime());
            System.out.println("4. Servings: " + draft.getServings());
            System.out.println("5. Ingredients:");
            for (Ingredient ingredient : draft.getIngredients()) {
                System.out.println("     - " + ingredient.getName() + ": " + ingredient.getAmount());
            }
            System.out.println("6. Steps:");
            for (int i = 0; i < draft.getInstructions().size(); i++) {
                System.out.println("     " + (i + 1) + ". " + draft.getInstructions().get(i));
            }
            System.out.println("7. Notes: " + draft.getNotes());
            System.out.println("8. Ask the AI for help");
            System.out.println("9. Save");
            System.out.println("0. Cancel");
            switch (ask("Choose: ")) {
                case "1" -> draft.setName(askOrKeep("Name", draft.getName()));
                case "2" -> draft.setMealType(askOrKeep("Meal type", draft.getMealType()));
                case "3" -> draft.setCookingTime(askOrKeep("Cooking time", draft.getCookingTime()));
                case "4" -> draft.setServings(askOrKeep("Servings", draft.getServings()));
                case "5" -> editIngredients(draft);
                case "6" -> editSteps(draft);
                case "7" -> draft.setNotes(askOrKeep("Notes", draft.getNotes()));
                case "8" -> askAI();
                case "9" -> {
                    if (recipeController.saveEdit()) {
                        System.out.println("Saved.");
                        return;
                    }
                    System.out.println("The recipe needs a name that no other recipe uses.");
                }
                case "0" -> {
                    recipeController.cancelEdit();
                    System.out.println("Changes discarded.");
                    return;
                }
                default -> System.out.println("Please type one of the numbers above.");
            }
        }
    }

    private void editIngredients(Recipe draft) {
        System.out.println("Type the ingredients one per line as name: amount, then END.");
        System.out.println("Type END straight away to keep the current list.");
        List<String> lines = readLines();
        if (lines.isEmpty()) {
            return;
        }
        List<Ingredient> ingredients = new ArrayList<>();
        for (String line : lines) {
            int colon = line.indexOf(':');
            if (colon < 0) {
                ingredients.add(new Ingredient(line, ""));
            } else {
                ingredients.add(new Ingredient(line.substring(0, colon).trim(), line.substring(colon + 1).trim()));
            }
        }
        draft.setIngredients(ingredients);
    }

    private void editSteps(Recipe draft) {
        System.out.println("Type the steps one per line, then END.");
        System.out.println("Type END straight away to keep the current steps.");
        List<String> lines = readLines();
        if (!lines.isEmpty()) {
            draft.setInstructions(lines);
        }
    }

    private void askAI() {
        String request = ask("What would you like to change or ask? ");
        if (request.isEmpty()) {
            return;
        }
        System.out.println("Thinking…");
        AIResult<Recipe> result = recipeController.requestModification(request);
        if (printResult(result) && result.getData() != null) {
            System.out.println("The recipe below has been updated. Choose 9 to save it.");
        }
    }

    private void addPersonalRecipe() {
        System.out.println("Paste or type your recipe, then type END on its own line.");
        List<String> lines = readLines();
        if (lines.isEmpty()) {
            return;
        }
        System.out.println("Thinking…");
        AIResult<Recipe> result = recipeController.addPersonalRecipe(String.join("\n", lines));
        if (printResult(result)) {
            System.out.println();
            System.out.println(formatRecipe(result.getData()));
        }
    }

    private void recommendMenu() {
        System.out.println();
        for (ChatMessage message : recipeController.getChatHistory()) {
            System.out.println((message.getAuthor().equals("AI") ? "AI: " : "You: ") + message.getContent());
        }
        System.out.println("Describe what you'd like to cook. Type view N to see a recipe, save N to save it,"
                + " or back to return.");
        List<Recipe> recipes = new ArrayList<>();
        while (true) {
            String input = ask("You: ");
            String command = input.toLowerCase();
            int number = command.length() > 5 ? toNumber(command.substring(5).trim()) : -1;
            if (command.equals("back")) {
                return;
            } else if ((command.startsWith("view ") || command.startsWith("save ")) && number > 0) {
                if (number > recipes.size()) {
                    System.out.println("There's no recipe " + number + " in the latest recommendations.");
                } else if (command.startsWith("view ")) {
                    System.out.println(formatRecipe(recipes.get(number - 1)));
                } else if (recipeController.saveRecommendedRecipe(recipes.get(number - 1))) {
                    System.out.println("Saved \"" + recipes.get(number - 1).getName() + "\" to your recipes.");
                } else {
                    System.out.println("You already have a recipe called \"" + recipes.get(number - 1).getName() + "\".");
                }
            } else if (!input.isEmpty()) {
                System.out.println("Thinking…");
                AIResult<List<Recipe>> result = recipeController.requestRecommendations(input);
                if (printResult(result) && !result.getData().isEmpty()) {
                    recipes = result.getData();
                    for (int i = 0; i < recipes.size(); i++) {
                        System.out.println("  " + (i + 1) + ". " + recipes.get(i).getName());
                    }
                }
            }
        }
    }

    private void mealPlanMenu() {
        while (true) {
            System.out.println();
            printWeek();
            System.out.println();
            System.out.println("1. Put a recipe in a slot");
            System.out.println("2. View a slot's recipe");
            System.out.println("3. Clear a slot");
            System.out.println("4. Generate a plan with AI");
            System.out.println("5. Clear the whole plan");
            System.out.println("6. Make a shopping list");
            System.out.println("0. Back");
            switch (ask("Choose: ")) {
                case "1" -> addToSlot();
                case "2" -> viewSlot();
                case "3" -> clearSlot();
                case "4" -> generatePlan();
                case "5" -> {
                    if (ask("Clear every meal from the week? (y/n): ").equalsIgnoreCase("y")) {
                        mealPlanController.clearPlan();
                    }
                }
                case "6" -> shoppingList();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Please type one of the numbers above.");
            }
        }
    }

    private void printWeek() {
        List<MealSlot> slots = mealPlanController.getSlots();
        for (DayOfWeek day : DayOfWeek.values()) {
            String line = String.format("%-10s", day.getDisplayName(TextStyle.FULL, Locale.getDefault()));
            for (MealTime time : MealTime.values()) {
                for (MealSlot slot : slots) {
                    if (slot.getDay() == day && slot.getMealTime() == time) {
                        line += " | " + mealTimeName(time) + ": " + slotName(slot.getRecipe());
                    }
                }
            }
            System.out.println(line);
        }
    }

    private String slotName(Recipe recipe) {
        if (recipe == null) {
            return "—";
        }
        return recipe.getName() + (recipe.getId() == null ? " (new)" : "");
    }

    private String mealTimeName(MealTime time) {
        return time.name().charAt(0) + time.name().substring(1).toLowerCase();
    }

    private MealSlot askSlot() {
        int day = toNumber(ask("Day (1 = Monday … 7 = Sunday): "));
        String meal = ask("Meal (b = breakfast, l = lunch, d = dinner): ").toLowerCase();
        MealTime time = null;
        if (meal.equals("b")) {
            time = MealTime.BREAKFAST;
        } else if (meal.equals("l")) {
            time = MealTime.LUNCH;
        } else if (meal.equals("d")) {
            time = MealTime.DINNER;
        }
        if (day >= 1 && day <= 7 && time != null) {
            for (MealSlot slot : mealPlanController.getSlots()) {
                if (slot.getDay() == DayOfWeek.of(day) && slot.getMealTime() == time) {
                    return slot;
                }
            }
        }
        System.out.println("That isn't a valid day and meal.");
        return null;
    }

    private void addToSlot() {
        MealSlot slot = askSlot();
        if (slot == null) {
            return;
        }
        Recipe recipe = browseRecipes("choose that recipe");
        if (recipe != null && !mealPlanController.assignRecipe(slot.getDay(), slot.getMealTime(), recipe.getId())) {
            System.out.println("That recipe no longer exists.");
        }
    }

    private void viewSlot() {
        MealSlot slot = askSlot();
        if (slot == null) {
            return;
        }
        System.out.println(slot.getRecipe() == null ? "That slot is empty." : formatRecipe(slot.getRecipe()));
    }

    private void clearSlot() {
        MealSlot slot = askSlot();
        if (slot != null) {
            mealPlanController.clearSlot(slot.getDay(), slot.getMealTime());
        }
    }

    private void generatePlan() {
        String criteria = ask("Describe what you want (goals, diet, time, …), or press Enter: ");
        boolean savedOnly = ask("Use only your saved recipes? (y/n): ").equalsIgnoreCase("y");
        System.out.println("Thinking…");
        printResult(mealPlanController.generateMealPlan(criteria, savedOnly));
    }

    private void shoppingList() {
        boolean planned = false;
        for (MealSlot slot : mealPlanController.getSlots()) {
            if (slot.getRecipe() != null) {
                planned = true;
            }
        }
        if (!planned) {
            System.out.println("Add some recipes to the meal plan first.");
            return;
        }
        boolean usePantry = ask("Take your pantry into account? (y/n): ").equalsIgnoreCase("y");
        System.out.println("Thinking…");
        AIResult<List<Ingredient>> result = mealPlanController.generateShoppingList(usePantry);
        if (printResult(result)) {
            System.out.println();
            System.out.println("Shopping list:");
            for (Ingredient ingredient : result.getData()) {
                System.out.println("  - " + ingredient.getName() + ": " + ingredient.getAmount());
            }
        }
    }

    private void pantryMenu() {
        while (true) {
            System.out.println();
            System.out.println("Pantry:");
            List<PantryItem> items = pantryController.getItems();
            if (items.isEmpty()) {
                System.out.println("(empty)");
            }
            for (PantryItem item : items) {
                System.out.println("  - " + item.getName() + ": " + item.getQuantity());
            }
            System.out.println("1. Add an item");
            System.out.println("2. Update an item's quantity");
            System.out.println("3. Remove an item");
            System.out.println("0. Back");
            switch (ask("Choose: ")) {
                case "1" -> {
                    if (!pantryController.addItem(ask("Name: "), ask("Quantity: "))) {
                        System.out.println("Enter a name and a quantity. The name must be new and at most "
                                + PantryManager.MAX_NAME_LENGTH + " characters long.");
                    }
                }
                case "2" -> {
                    if (!pantryController.updateItem(ask("Name: "), ask("New quantity: "))) {
                        System.out.println("There's no item with that name, or the quantity is blank.");
                    }
                }
                case "3" -> {
                    if (!pantryController.removeItem(ask("Name: "))) {
                        System.out.println("There's no item with that name.");
                    }
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("Please type one of the numbers above.");
            }
        }
    }

    private void profileMenu() {
        while (true) {
            System.out.println();
            System.out.println("Profile (the AI uses it for recommendations, recipe changes and meal plans):");
            Map<String, String> settings = profileController.getSettings();
            List<String> names = new ArrayList<>(settings.keySet());
            for (int i = 0; i < names.size(); i++) {
                System.out.println((i + 1) + ". " + names.get(i) + ": " + settings.get(names.get(i)));
            }
            System.out.println("0. Back");
            String choice = ask("Choose a setting to edit: ");
            if (choice.equals("0")) {
                return;
            }
            int number = toNumber(choice);
            if (number >= 1 && number <= names.size()) {
                String name = names.get(number - 1);
                profileController.updateSetting(name, ask("New value for " + name + " (leave blank to clear): "));
            } else {
                System.out.println("Please type one of the numbers above.");
            }
        }
    }

    private boolean printResult(AIResult<?> result) {
        if (result.isSuccess() || result.isFlagged()) {
            System.out.println("AI: " + result.getMessage());
        } else {
            System.out.println("Something went wrong: " + result.getMessage());
        }
        return result.isSuccess();
    }

    private String formatRecipe(Recipe recipe) {
        String text = "== " + recipe.getName() + (recipe.isFavourite() ? " ★" : "") + " ==\n"
                + "Meal type: " + recipe.getMealType() + " | Cooking time: " + recipe.getCookingTime()
                + " | Servings: " + recipe.getServings() + "\nIngredients:\n";
        for (Ingredient ingredient : recipe.getIngredients()) {
            text += "  - " + ingredient.getName() + ": " + ingredient.getAmount() + "\n";
        }
        text += "Steps:\n";
        for (int i = 0; i < recipe.getInstructions().size(); i++) {
            text += "  " + (i + 1) + ". " + recipe.getInstructions().get(i) + "\n";
        }
        return text + "Notes: " + recipe.getNotes();
    }

    private String askOrKeep(String label, String current) {
        String value = ask(label + " (press Enter to keep \"" + current + "\"): ");
        return value.isEmpty() ? current : value;
    }

    private List<String> readLines() {
        List<String> lines = new ArrayList<>();
        String line = ask("");
        while (!line.equalsIgnoreCase("END")) {
            if (!line.isEmpty()) {
                lines.add(line);
            }
            line = ask("");
        }
        return lines;
    }

    private int toNumber(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }
}
