package recipeagent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

public class RecipeListPanel extends JPanel {
    private RecipeController controller;
    private DefaultListModel<Recipe> listModel = new DefaultListModel<>();
    private JList<Recipe> list = new JList<>(listModel);
    private JTextField searchField = new JTextField();
    private JButton searchButton = new JButton("Search");
    private JButton previousButton = new JButton("< Prev");
    private JButton nextButton = new JButton("Next >");
    private JTextArea messageArea = new JTextArea(3, 20);

    public RecipeListPanel(RecipeController controller, Consumer<Recipe> onSelect) {
        super(new BorderLayout(5, 5));
        this.controller = controller;
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focused) {
                Recipe recipe = (Recipe) value;
                String text = (recipe.isFavourite() ? "★ " : "    ") + recipe.getName();
                return super.getListCellRendererComponent(list, text, index, selected, focused);
            }
        });
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelect.accept(list.getSelectedValue());
            }
        });
        JButton clearButton = new JButton("Clear");
        searchButton.addActionListener(e -> enterSearch());
        searchField.addActionListener(e -> enterSearch());
        clearButton.addActionListener(e -> clearSearch());
        previousButton.addActionListener(e -> showPage(controller.previousPage()));
        nextButton.addActionListener(e -> showPage(controller.nextPage()));
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);

        JPanel searchButtons = new JPanel();
        searchButtons.add(searchButton);
        searchButtons.add(clearButton);
        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(searchButtons, BorderLayout.EAST);
        JPanel pageRow = new JPanel();
        pageRow.add(previousButton);
        pageRow.add(nextButton);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(pageRow, BorderLayout.NORTH);
        bottom.add(messageArea, BorderLayout.CENTER);
        add(searchRow, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void openRecipeList() {
        showPage(controller.firstPage());
    }

    public void refresh() {
        list.repaint();
    }

    public void showStatus(String text) {
        messageArea.setForeground(UIManager.getColor("Label.foreground"));
        messageArea.setText(text);
    }

    public void showResult(AIResult<?> result) {
        if (!result.isSuccess() && !result.isFlagged()) {
            messageArea.setText("");
            JOptionPane.showMessageDialog(this, result.getMessage(), "Something went wrong", JOptionPane.ERROR_MESSAGE);
            return;
        }
        showStatus(result.getMessage());
        if (result.isFlagged()) {
            messageArea.setForeground(Color.RED);
        }
    }

    private void enterSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            return;
        }
        searchButton.setEnabled(false);
        showStatus("Searching your recipes…");
        MainWindow.runInBackground(() -> controller.searchRecipes(query), result -> {
            searchButton.setEnabled(true);
            if (result.isSuccess()) {
                showPage(result.getData());
            }
            showResult(result);
        });
    }

    private void clearSearch() {
        searchField.setText("");
        showStatus("");
        showPage(controller.clearSearch());
    }

    private void showPage(List<Recipe> page) {
        listModel.clear();
        listModel.addAll(page);
        previousButton.setEnabled(controller.hasPreviousPage());
        nextButton.setEnabled(controller.hasNextPage());
    }
}
