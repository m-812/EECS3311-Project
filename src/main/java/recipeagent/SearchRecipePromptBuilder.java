package recipeagent;

public class SearchRecipePromptBuilder extends PromptBuilder {
    private static final String TASK_PROMPT = """
            Task: the user's message describes the kind of recipe they are looking for. Find the saved recipes below that match it.
            Add an "ids" field with a list of the matching recipes' id values, best match first. Use [] if none match.""";

    @Override
    public void buildTaskInstructions() {
        prompt.addSection(TASK_PROMPT);
    }
}
