package recipeagent;

public class RecommendedRecipePromptBuilder extends PromptBuilder {
    private static final String TASK_PROMPT = """
            Task: recommend recipes that fit what the user asks for in this conversation. Use their profile, pantry and favourites below when they are given.
            Add a "recipes" field with a list of 1 to 3 recipes. In message, name the recipes you picked.
            If you need more details or nothing fits, return an empty list and ask a question or suggest alternatives in message.""";

    @Override
    public void buildTaskInstructions() {
        prompt.addSection(TASK_PROMPT + "\n" + RECIPE_FORMAT);
    }
}
