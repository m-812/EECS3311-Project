package recipeagent;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PantryPanel extends JPanel {
    private PantryController controller;
    private DefaultTableModel tableModel = new DefaultTableModel(new String[] {"Name", "Quantity"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private JTable table = new JTable(tableModel);
    private JTextField nameField = new JTextField(15);
    private JTextField quantityField = new JTextField(10);

    public PantryPanel(PantryController controller) {
        super(new BorderLayout());
        this.controller = controller;
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update quantity");
        JButton removeButton = new JButton("Remove");
        addButton.addActionListener(e -> addItem());
        updateButton.addActionListener(e -> updateItem());
        removeButton.addActionListener(e -> removeItem());
        table.getSelectionModel().addListSelectionListener(e -> selectItem());

        JPanel form = new JPanel();
        form.add(new JLabel("Name"));
        form.add(nameField);
        form.add(new JLabel("Quantity"));
        form.add(quantityField);
        form.add(addButton);
        form.add(updateButton);
        form.add(removeButton);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                openPantry();
            }
        });
        openPantry();
    }

    public void openPantry() {
        tableModel.setRowCount(0);
        for (PantryItem item : controller.getItems()) {
            tableModel.addRow(new Object[] {item.getName(), item.getQuantity()});
        }
    }

    private void selectItem() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            nameField.setText((String) tableModel.getValueAt(row, 0));
            quantityField.setText((String) tableModel.getValueAt(row, 1));
        }
    }

    private void addItem() {
        if (controller.addItem(nameField.getText().trim(), quantityField.getText().trim())) {
            nameField.setText("");
            quantityField.setText("");
        } else {
            showError("Enter a name and a quantity. The name must be new and at most "
                    + PantryManager.MAX_NAME_LENGTH + " characters long.");
        }
        openPantry();
    }

    private void updateItem() {
        if (!controller.updateItem(nameField.getText().trim(), quantityField.getText().trim())) {
            showError("Pick an item from the list and enter its new quantity.");
        }
        openPantry();
    }

    private void removeItem() {
        if (controller.removeItem(nameField.getText().trim())) {
            nameField.setText("");
            quantityField.setText("");
        } else {
            showError("Pick an item from the list to remove.");
        }
        openPantry();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Pantry", JOptionPane.ERROR_MESSAGE);
    }
}
