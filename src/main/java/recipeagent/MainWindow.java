package recipeagent;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

public class MainWindow extends JFrame {
    public MainWindow(AIAgent agent) {
        super("AI Recipe & Meal Planner");
        RecipeController recipeController = new RecipeController(agent);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Recipes", new RecipesPanel(recipeController));
        tabs.addTab("Recommend", new RecommendPanel(recipeController));
        tabs.addTab("Pantry", new PantryPanel(new PantryController()));
        tabs.addTab("Profile", new ProfilePanel(new ProfileController()));
        add(tabs);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static <T> void runInBackground(Supplier<T> task, Consumer<T> whenDone) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return task.get();
            }

            @Override
            protected void done() {
                try {
                    whenDone.accept(get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }
}
