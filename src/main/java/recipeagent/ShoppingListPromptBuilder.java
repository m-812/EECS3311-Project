package recipeagent;

public class ShoppingListPromptBuilder extends PromptBuilder {
    private static final String TASK_PROMPT = """
            Task: make a shopping list for the planned recipes below. A recipe listed more than once is cooked more than once. Combine the same ingredient across recipes and total the amounts.
            If a pantry list is given, leave out what the user already has enough of and lower the amounts of what they only have some of.
            Add an "ingredients" field with a list like [{"name": "Rice", "amount": "2 cups"}]. Use [] if nothing needs to be bought, and say so in message. If a recipe has no ingredient information, tell the user in message.""";

    @Override
    public void buildTaskInstructions() {
        prompt.addSection(TASK_PROMPT);
    }
}
