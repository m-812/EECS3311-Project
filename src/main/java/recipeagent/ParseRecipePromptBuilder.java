package recipeagent;

public class ParseRecipePromptBuilder extends PromptBuilder {
    private static final String TASK_PROMPT = """
            Task: the user's message is a recipe they wrote or pasted in. Organise it into the recipe format below, keeping their wording.
            Don't add ingredients or steps they didn't write, and leave a field blank when the text doesn't say.
            Add a "recipe" field holding the recipe. If the text isn't a recipe, set flagged to true.""";

    @Override
    public void buildTaskInstructions() {
        prompt.addSection(TASK_PROMPT + "\n" + RECIPE_FORMAT);
    }
}
