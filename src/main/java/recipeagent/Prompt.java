package recipeagent;

import java.util.ArrayList;
import java.util.List;

public class Prompt {
    private List<String> sections;

    public Prompt() {
        sections = new ArrayList<>();
    }

    public void addSection(String text) {
        sections.add(text);
    }

    public String toText() {
        return String.join("\n\n", sections);
    }
}
