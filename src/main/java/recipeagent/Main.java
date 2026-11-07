package recipeagent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        List<String> options = List.of(args);
        String apiKey = readApiKey();
        boolean fakeAI = options.contains("--fake-ai");
        AIAgent agent = new AIAgent(fakeAI ? new FakeLLMClient() : new HttpLLMClient(apiKey, "claude-sonnet-5-5"));
        if (options.contains("--fixtures")) {
            loadFixtures();
        }
        if (options.contains("--cli")) {
            if (apiKey.isEmpty() && !fakeAI) {
                System.out.println("No LLM_API_KEY was found in the .env file, so the AI features won't work.");
            }
            new CommandLineApp(agent).run();
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow(agent);
            window.setVisible(true);
            if (apiKey.isEmpty() && !fakeAI) {
                JOptionPane.showMessageDialog(window,
                        "No LLM_API_KEY was found in the .env file, so the AI features won't work.",
                        "No API key", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private static String readApiKey() {
        try {
            for (String line : Files.readAllLines(Path.of(".env"))) {
                if (line.startsWith("LLM_API_KEY=")) {
                    return line.substring("LLM_API_KEY=".length()).trim();
                }
            }
        } catch (IOException e) {
        }
        return "";
    }

    private static void loadFixtures() {
        try {
            List<Recipe> recipes = new Gson().fromJson(Files.readString(Path.of("sample-data/fixtures.json")),
                    new TypeToken<List<Recipe>>() {});
            for (Recipe recipe : recipes) {
                RecipeManager.getInstance().saveRecipe(recipe);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
