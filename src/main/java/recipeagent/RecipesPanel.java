package recipeagent;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class RecipesPanel extends JPanel {
    private RecipeController controller;
    private RecipeListPanel listPanel;
    private JTextArea details = new JTextArea();
    private JButton addButton = new JButton("Add personal recipe");
    private JButton favouriteButton = new JButton("Favourite");
    private JButton editButton = new JButton("Edit");
    private JButton removeButton = new JButton("Remove");
    private Recipe selected;

    public RecipesPanel(RecipeController controller) {
        super(new BorderLayout(5, 5));
        this.controller = controller;
        listPanel = new RecipeListPanel(controller, this::selectRecipe);
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setMargin(new Insets(8, 8, 8, 8));
        addButton.addActionListener(e -> pasteRecipe());
        favouriteButton.addActionListener(e -> clickFavourite());
        editButton.addActionListener(e -> clickEdit());
        removeButton.addActionListener(e -> removeRecipe());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, new JScrollPane(details));
        split.setDividerLocation(380);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addButton);
        buttons.add(favouriteButton);
        buttons.add(editButton);
        buttons.add(removeButton);
        add(split, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                openRecipeList();
            }
        });
        openRecipeList();
    }

    public void openRecipeList() {
        listPanel.openRecipeList();
    }

    private void selectRecipe(Recipe recipe) {
        selected = recipe;
        details.setText(recipe == null ? "" : formatRecipe(recipe));
        details.setCaretPosition(0);
        favouriteButton.setEnabled(recipe != null);
        editButton.setEnabled(recipe != null);
        removeButton.setEnabled(recipe != null);
        favouriteButton.setText(recipe != null && recipe.isFavourite() ? "Unfavourite" : "Favourite");
    }

    private void clickFavourite() {
        if (controller.setFavourite(selected.getId(), !selected.isFavourite())) {
            selectRecipe(selected);
            listPanel.refresh();
        } else {
            JOptionPane.showMessageDialog(this, "That recipe no longer exists.", "Favourite", JOptionPane.ERROR_MESSAGE);
            openRecipeList();
        }
    }

    private void clickEdit() {
        RecipeEditPanel.open(this, controller, selected.getId());
        selectRecipe(selected);
        listPanel.refresh();
    }

    private void removeRecipe() {
        int choice = JOptionPane.showConfirmDialog(this, "Remove \"" + selected.getName() + "\" from your recipes?",
                "Remove recipe", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        if (!controller.removeRecipe(selected.getId())) {
            JOptionPane.showMessageDialog(this, "That recipe no longer exists.", "Remove recipe",
                    JOptionPane.ERROR_MESSAGE);
        }
        openRecipeList();
    }

    private void pasteRecipe() {
        JTextArea text = new JTextArea(20, 60);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        int choice = JOptionPane.showConfirmDialog(this, new JScrollPane(text), "Paste your recipe",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION || text.getText().isBlank()) {
            return;
        }
        addButton.setEnabled(false);
        listPanel.showStatus("Reading your recipe…");
        MainWindow.runInBackground(() -> controller.addPersonalRecipe(text.getText()), result -> {
            addButton.setEnabled(true);
            if (result.isSuccess()) {
                openRecipeList();
            }
            listPanel.showResult(result);
        });
    }

    static String formatRecipe(Recipe recipe) {
        String text = recipe.getName() + (recipe.isFavourite() ? "  ★" : "") + "\n"
                + "Meal type: " + recipe.getMealType() + "    Cooking time: " + recipe.getCookingTime()
                + "    Servings: " + recipe.getServings() + "\n\nIngredients\n";
        for (Ingredient ingredient : recipe.getIngredients()) {
            text += "- " + ingredient.getName() + ": " + ingredient.getAmount() + "\n";
        }
        text += "\nSteps\n";
        for (int i = 0; i < recipe.getInstructions().size(); i++) {
            text += (i + 1) + ". " + recipe.getInstructions().get(i) + "\n";
        }
        return text + "\nNotes\n" + recipe.getNotes();
    }
}
