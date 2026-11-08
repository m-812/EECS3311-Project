package recipeagent;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class RecommendPanel extends JPanel {
    private RecipeController controller;
    private JTextArea chatArea = new JTextArea();
    private JTextField requestField = new JTextField();
    private JButton sendButton = new JButton("Send");
    private DefaultListModel<Recipe> recipeModel = new DefaultListModel<>();
    private JList<Recipe> recipeList = new JList<>(recipeModel);
    private JTextArea details = new JTextArea();
    private JButton saveButton = new JButton("Save to my recipes");
    private JTextArea status = new JTextArea(2, 20);

    public RecommendPanel(RecipeController controller) {
        super(new BorderLayout(5, 5));
        this.controller = controller;
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new Insets(8, 8, 8, 8));
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setMargin(new Insets(8, 8, 8, 8));
        status.setEditable(false);
        status.setLineWrap(true);
        status.setWrapStyleWord(true);
        status.setOpaque(false);
        recipeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recipeList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focused) {
                return super.getListCellRendererComponent(list, ((Recipe) value).getName(), index, selected, focused);
            }
        });
        recipeList.addListSelectionListener(e -> showRecipe(recipeList.getSelectedValue()));
        sendButton.addActionListener(e -> requestRecommendations());
        requestField.addActionListener(e -> requestRecommendations());
        saveButton.addActionListener(e -> saveRecommendedRecipe());
        saveButton.setEnabled(false);

        JPanel askRow = new JPanel(new BorderLayout(5, 5));
        askRow.add(requestField, BorderLayout.CENTER);
        askRow.add(sendButton, BorderLayout.EAST);
        JPanel chat = new JPanel(new BorderLayout(5, 5));
        chat.add(new JLabel("Tell the AI what you'd like to cook: ingredients, time, diet, cuisine…"),
                BorderLayout.NORTH);
        chat.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chat.add(askRow, BorderLayout.SOUTH);

        JSplitPane recipeSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(recipeList),
                new JScrollPane(details));
        recipeSplit.setDividerLocation(120);
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow.add(saveButton);
        JPanel saveRow = new JPanel(new BorderLayout());
        saveRow.add(buttonRow, BorderLayout.NORTH);
        saveRow.add(status, BorderLayout.CENTER);
        JPanel recipes = new JPanel(new BorderLayout(5, 5));
        recipes.add(new JLabel("Recommended recipes"), BorderLayout.NORTH);
        recipes.add(recipeSplit, BorderLayout.CENTER);
        recipes.add(saveRow, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chat, recipes);
        split.setDividerLocation(500);
        add(split, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                showChat();
            }
        });
        showChat();
    }

    private void showChat() {
        String text = "";
        for (ChatMessage message : controller.getChatHistory()) {
            text += (message.getAuthor().equals("AI") ? "AI: " : "You: ") + message.getContent() + "\n\n";
        }
        chatArea.setText(text);
    }

    private void showRecipe(Recipe recipe) {
        details.setText(recipe == null ? "" : RecipesPanel.formatRecipe(recipe));
        details.setCaretPosition(0);
        saveButton.setEnabled(recipe != null);
        status.setText("");
    }

    private void requestRecommendations() {
        String request = requestField.getText().trim();
        if (request.isEmpty()) {
            return;
        }
        requestField.setText("");
        setBusy(true);
        chatArea.append("You: " + request + "\n\n");
        MainWindow.runInBackground(() -> controller.requestRecommendations(request), result -> {
            setBusy(false);
            showChat();
            if (!result.isSuccess() && !result.isFlagged()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Something went wrong",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (result.isSuccess() && !result.getData().isEmpty()) {
                recipeModel.clear();
                recipeModel.addAll(result.getData());
                recipeList.setSelectedIndex(0);
            }
        });
    }

    private void setBusy(boolean busy) {
        sendButton.setText(busy ? "Thinking…" : "Send");
        sendButton.setEnabled(!busy);
        requestField.setEnabled(!busy);
    }

    private void saveRecommendedRecipe() {
        Recipe recipe = recipeList.getSelectedValue();
        if (controller.saveRecommendedRecipe(recipe)) {
            status.setText("Saved \"" + recipe.getName() + "\" to your recipes.");
        } else {
            status.setText("You already have a recipe called \"" + recipe.getName() + "\".");
        }
    }
}
