package recipeagent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class MealPlanPanel extends JPanel {
    private MealPlanController controller;
    private RecipeController recipeController;
    private JPanel grid = new JPanel(new GridLayout(3, 8, 4, 4));
    private JTextField criteriaField = new JTextField(30);
    private JCheckBox savedOnlyBox = new JCheckBox("Saved recipes only");
    private JCheckBox usePantryBox = new JCheckBox("Use my pantry", true);
    private JButton generateButton = new JButton("Generate plan");
    private JButton clearPlanButton = new JButton("Clear plan");
    private JButton shoppingButton = new JButton("Shopping list");
    private JTextArea messageArea = new JTextArea(3, 40);

    public MealPlanPanel(MealPlanController controller, RecipeController recipeController) {
        super(new BorderLayout(5, 5));
        this.controller = controller;
        this.recipeController = recipeController;
        generateButton.addActionListener(e -> generatePlan());
        criteriaField.addActionListener(e -> generatePlan());
        clearPlanButton.addActionListener(e -> clearPlan());
        shoppingButton.addActionListener(e -> clickShoppingList());
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);
        messageArea.setMargin(new Insets(4, 6, 4, 6));

        JPanel header = new JPanel(new GridLayout(1, 8, 4, 4));
        header.add(new JLabel(""));
        for (DayOfWeek day : DayOfWeek.values()) {
            header.add(new JLabel(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()), SwingConstants.CENTER));
        }
        JPanel calendar = new JPanel(new BorderLayout(4, 4));
        calendar.add(header, BorderLayout.NORTH);
        calendar.add(grid, BorderLayout.CENTER);

        JPanel planRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        planRow.add(new JLabel("Plan with AI:"));
        planRow.add(criteriaField);
        planRow.add(savedOnlyBox);
        planRow.add(generateButton);
        planRow.add(clearPlanButton);
        JPanel listRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        listRow.add(new JLabel("Shopping list:"));
        listRow.add(usePantryBox);
        listRow.add(shoppingButton);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(planRow, BorderLayout.NORTH);
        bottom.add(listRow, BorderLayout.CENTER);
        bottom.add(messageArea, BorderLayout.SOUTH);
        add(calendar, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                openMealPlan();
            }
        });
        openMealPlan();
    }

    public void openMealPlan() {
        grid.removeAll();
        List<MealSlot> slots = controller.getSlots();
        for (MealTime time : MealTime.values()) {
            grid.add(new JLabel(mealTimeName(time)));
            for (DayOfWeek day : DayOfWeek.values()) {
                for (MealSlot slot : slots) {
                    if (slot.getDay() == day && slot.getMealTime() == time) {
                        grid.add(slotButton(slot));
                    }
                }
            }
        }
        grid.revalidate();
        grid.repaint();
    }

    private JButton slotButton(MealSlot slot) {
        Recipe recipe = slot.getRecipe();
        String text = "—";
        if (recipe != null) {
            text = "<html><center>" + recipe.getName() + (recipe.getId() == null ? "<br><i>(new)</i>" : "")
                    + "</center></html>";
        }
        JButton button = new JButton(text);
        button.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem choose = new JMenuItem("Choose recipe…");
            choose.addActionListener(x -> addToSlot(slot.getDay(), slot.getMealTime()));
            menu.add(choose);
            if (recipe != null) {
                JMenuItem view = new JMenuItem("View recipe");
                view.addActionListener(x -> viewRecipe(recipe));
                menu.add(view);
                JMenuItem clear = new JMenuItem("Clear");
                clear.addActionListener(x -> {
                    controller.clearSlot(slot.getDay(), slot.getMealTime());
                    openMealPlan();
                });
                menu.add(clear);
            }
            menu.show(button, 0, button.getHeight());
        });
        return button;
    }

    private String mealTimeName(MealTime time) {
        return time.name().charAt(0) + time.name().substring(1).toLowerCase();
    }

    private void addToSlot(DayOfWeek day, MealTime time) {
        RecipeListPanel picker = new RecipeListPanel(recipeController, recipe -> {
        });
        picker.setPreferredSize(new Dimension(420, 580));
        picker.openRecipeList();
        String title = "Choose a recipe for " + day.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " "
                + mealTimeName(time).toLowerCase();
        int choice = JOptionPane.showConfirmDialog(this, picker, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        Recipe recipe = picker.getSelectedRecipe();
        if (choice != JOptionPane.OK_OPTION || recipe == null) {
            return;
        }
        if (!controller.assignRecipe(day, time, recipe.getId())) {
            JOptionPane.showMessageDialog(this, "That recipe no longer exists.", "Meal plan", JOptionPane.ERROR_MESSAGE);
        }
        openMealPlan();
    }

    private void viewRecipe(Recipe recipe) {
        JTextArea text = new JTextArea(RecipesPanel.formatRecipe(recipe), 20, 45);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(text), recipe.getName(), JOptionPane.PLAIN_MESSAGE);
    }

    private void clearPlan() {
        int choice = JOptionPane.showConfirmDialog(this, "Clear every meal from the week?", "Clear plan",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            controller.clearPlan();
            openMealPlan();
        }
    }

    private void generatePlan() {
        if (!generateButton.isEnabled()) {
            return;
        }
        setBusy(true);
        showStatus("Planning your week…");
        String criteria = criteriaField.getText().trim();
        boolean savedOnly = savedOnlyBox.isSelected();
        MainWindow.runInBackground(() -> controller.generateMealPlan(criteria, savedOnly), result -> {
            setBusy(false);
            if (result.isSuccess()) {
                openMealPlan();
            }
            showResult(result);
        });
    }

    private void clickShoppingList() {
        boolean planned = false;
        for (MealSlot slot : controller.getSlots()) {
            if (slot.getRecipe() != null) {
                planned = true;
            }
        }
        if (!planned) {
            showStatus("Add some recipes to the meal plan first.");
            return;
        }
        setBusy(true);
        showStatus("Making your shopping list…");
        boolean usePantry = usePantryBox.isSelected();
        MainWindow.runInBackground(() -> controller.generateShoppingList(usePantry), result -> {
            setBusy(false);
            showResult(result);
            if (result.isSuccess()) {
                String text = result.getMessage() + "\n\n";
                for (Ingredient ingredient : result.getData()) {
                    text += "- " + ingredient.getName() + ": " + ingredient.getAmount() + "\n";
                }
                JTextArea list = new JTextArea(text, 20, 40);
                list.setEditable(false);
                list.setLineWrap(true);
                list.setWrapStyleWord(true);
                JOptionPane.showMessageDialog(this, new JScrollPane(list), "Shopping list", JOptionPane.PLAIN_MESSAGE);
            }
        });
    }

    private void setBusy(boolean busy) {
        generateButton.setEnabled(!busy);
        clearPlanButton.setEnabled(!busy);
        shoppingButton.setEnabled(!busy);
    }

    private void showStatus(String text) {
        messageArea.setForeground(UIManager.getColor("Label.foreground"));
        messageArea.setText(text);
    }

    private void showResult(AIResult<?> result) {
        if (!result.isSuccess() && !result.isFlagged()) {
            showStatus("");
            JOptionPane.showMessageDialog(this, result.getMessage(), "Something went wrong", JOptionPane.ERROR_MESSAGE);
            return;
        }
        showStatus(result.getMessage());
        if (result.isFlagged()) {
            messageArea.setForeground(Color.RED);
        }
    }
}
