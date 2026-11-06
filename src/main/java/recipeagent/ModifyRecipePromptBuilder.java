package recipeagent;

public class ModifyRecipePromptBuilder extends PromptBuilder {
    private static final String TASK_PROMPT = """
            Task: help the user change the recipe below, which they are editing. They may ask about ingredient substitutions, serving sizes or cooking changes. Put your advice in message.
            Add a "recipe" field. When the user asks you to apply a change, set it to the whole updated recipe, keeping the name unless they ask to rename it. Otherwise set it to null.""";

    @Override
    public void buildTaskInstructions() {
        prompt.addSection(TASK_PROMPT + "\n" + RECIPE_FORMAT);
    }
}
