package recipeagent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainWindow extends JFrame {
    public MainWindow(AIAgent agent) {
        super("AI Recipe & Meal Planner");
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Pantry", new PantryPanel(new PantryController()));
        tabs.addTab("Profile", new ProfilePanel(new ProfileController()));
        add(tabs);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
