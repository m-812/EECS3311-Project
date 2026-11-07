package recipeagent;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class RecipeEditPanel extends JPanel {
    private RecipeController controller;
    private Recipe draft;
    private Runnable onClose = () -> {
    };
    private JTextField nameField = new JTextField(25);
    private JTextField mealTypeField = new JTextField(10);
    private JTextField cookingTimeField = new JTextField(10);
    private JTextField servingsField = new JTextField(5);
    private DefaultTableModel ingredientModel = new DefaultTableModel(new String[] {"Name", "Amount"}, 0);
    private JTable ingredientTable = new JTable(ingredientModel);
    private JTextArea stepsArea = new JTextArea(6, 30);
    private JTextArea notesArea = new JTextArea(3, 30);
    private JTextArea chatArea = new JTextArea();
    private JTextField requestField = new JTextField();
    private JButton askButton = new JButton("Ask");
    private JButton saveButton = new JButton("Save");
    private JButton cancelButton = new JButton("Cancel");
    private boolean busy = false;

    public RecipeEditPanel(RecipeController controller, String recipeId) {
        super(new BorderLayout(5, 5));
        this.controller = controller;
        draft = controller.startEditing(recipeId);

        JButton addRowButton = new JButton("+ Row");
        JButton removeRowButton = new JButton("- Row");
        addRowButton.addActionListener(e -> ingredientModel.addRow(new Object[] {"", ""}));
        removeRowButton.addActionListener(e -> removeIngredientRow());
        askButton.addActionListener(e -> askAI());
        requestField.addActionListener(e -> askAI());
        saveButton.addActionListener(e -> clickSave());
        cancelButton.addActionListener(e -> cancelEdit());
        ingredientTable.setPreferredScrollableViewportSize(new Dimension(300, 120));
        stepsArea.setLineWrap(true);
        stepsArea.setWrapStyleWord(true);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridy = 0;
        addField(form, c, "Name", nameField, 0);
        addField(form, c, "Meal type", mealTypeField, 0);
        addField(form, c, "Cooking time", cookingTimeField, 0);
        addField(form, c, "Servings", servingsField, 0);
        JPanel rowButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rowButtons.add(addRowButton);
        rowButtons.add(removeRowButton);
        JPanel ingredients = new JPanel(new BorderLayout());
        ingredients.add(new JScrollPane(ingredientTable), BorderLayout.CENTER);
        ingredients.add(rowButtons, BorderLayout.SOUTH);
        addField(form, c, "Ingredients", ingredients, 1);
        addField(form, c, "Steps (one per line)", new JScrollPane(stepsArea), 1);
        addField(form, c, "Notes", new JScrollPane(notesArea), 0.3);

        JPanel askRow = new JPanel(new BorderLayout(5, 5));
        askRow.add(requestField, BorderLayout.CENTER);
        askRow.add(askButton, BorderLayout.EAST);
        JPanel chat = new JPanel(new BorderLayout(5, 5));
        chat.add(new JLabel("Ask the AI for substitutions, serving sizes or other changes"), BorderLayout.NORTH);
        chat.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chat.add(askRow, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, chat);
        split.setDividerLocation(560);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(saveButton);
        buttons.add(cancelButton);
        add(split, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        loadForm();
    }

    public static void open(Component parent, RecipeController controller, String recipeId) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Edit recipe",
                Dialog.ModalityType.APPLICATION_MODAL);
        RecipeEditPanel panel = new RecipeEditPanel(controller, recipeId);
        panel.onClose = dialog::dispose;
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                panel.cancelEdit();
            }
        });
        dialog.add(panel);
        dialog.setSize(1000, 650);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private void addField(JPanel form, GridBagConstraints c, String label, Component field, double weighty) {
        c.gridx = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        c.weighty = weighty;
        c.fill = weighty > 0 ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        form.add(field, c);
        c.gridy++;
    }

    private void loadForm() {
        nameField.setText(draft.getName());
        mealTypeField.setText(draft.getMealType());
        cookingTimeField.setText(draft.getCookingTime());
        servingsField.setText(draft.getServings());
        ingredientModel.setRowCount(0);
        for (Ingredient ingredient : draft.getIngredients()) {
            ingredientModel.addRow(new Object[] {ingredient.getName(), ingredient.getAmount()});
        }
        stepsArea.setText(String.join("\n", draft.getInstructions()));
        notesArea.setText(draft.getNotes());
    }

    private void saveForm() {
        stopTableEditing();
        draft.setName(nameField.getText().trim());
        draft.setMealType(mealTypeField.getText().trim());
        draft.setCookingTime(cookingTimeField.getText().trim());
        draft.setServings(servingsField.getText().trim());
        List<Ingredient> ingredients = new ArrayList<>();
        for (int row = 0; row < ingredientModel.getRowCount(); row++) {
            String name = ((String) ingredientModel.getValueAt(row, 0)).trim();
            if (!name.isEmpty()) {
                ingredients.add(new Ingredient(name, ((String) ingredientModel.getValueAt(row, 1)).trim()));
            }
        }
        draft.setIngredients(ingredients);
        List<String> steps = new ArrayList<>();
        for (String line : stepsArea.getText().split("\n")) {
            if (!line.isBlank()) {
                steps.add(line.trim());
            }
        }
        draft.setInstructions(steps);
        draft.setNotes(notesArea.getText().trim());
    }

    private void stopTableEditing() {
        if (ingredientTable.isEditing()) {
            ingredientTable.getCellEditor().stopCellEditing();
        }
    }

    private void removeIngredientRow() {
        stopTableEditing();
        int row = ingredientTable.getSelectedRow();
        if (row >= 0) {
            ingredientModel.removeRow(row);
        }
    }

    private void askAI() {
        String request = requestField.getText().trim();
        if (request.isEmpty() || busy) {
            return;
        }
        saveForm();
        setBusy(true);
        chatArea.append("You: " + request + "\n\n");
        requestField.setText("");
        MainWindow.runInBackground(() -> controller.requestModification(request), result -> {
            setBusy(false);
            if (!result.isSuccess() && !result.isFlagged()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Something went wrong",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            chatArea.append("AI: " + result.getMessage() + "\n\n");
            if (result.isSuccess() && result.getData() != null) {
                loadForm();
                chatArea.append("(The form has been updated. Select Save to keep the changes.)\n\n");
            }
        });
    }

    private void setBusy(boolean busy) {
        this.busy = busy;
        askButton.setText(busy ? "Thinking…" : "Ask");
        askButton.setEnabled(!busy);
        requestField.setEnabled(!busy);
        saveButton.setEnabled(!busy);
        cancelButton.setEnabled(!busy);
    }

    private void clickSave() {
        saveForm();
        if (controller.saveEdit()) {
            onClose.run();
        } else {
            JOptionPane.showMessageDialog(this, "The recipe needs a name that no other recipe uses.",
                    "Can't save", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelEdit() {
        if (!busy) {
            controller.cancelEdit();
            onClose.run();
        }
    }
}
