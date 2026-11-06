package recipeagent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        String apiKey = readApiKey();
        AIAgent agent = new AIAgent(new HttpLLMClient(apiKey, "claude-sonnet-5-5"));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow(agent);
            window.setVisible(true);
            if (apiKey.isEmpty()) {
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
}
