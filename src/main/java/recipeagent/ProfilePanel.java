package recipeagent;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ProfilePanel extends JPanel {
    private ProfileController controller;
    private Map<String, JTextField> fields = new LinkedHashMap<>();
    private JLabel status = new JLabel(" ");

    public ProfilePanel(ProfileController controller) {
        super(new BorderLayout());
        this.controller = controller;
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        form.add(new JLabel("The AI uses your profile for recommendations, recipe changes and meal plans."
                + " Leave a field blank to skip it."), c);
        c.gridwidth = 1;
        for (String name : controller.getSettings().keySet()) {
            JTextField field = new JTextField(40);
            fields.put(name, field);
            c.gridy++;
            c.gridx = 0;
            form.add(new JLabel(name), c);
            c.gridx = 1;
            form.add(field, c);
        }
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveProfile());
        JPanel saveRow = new JPanel();
        saveRow.add(saveButton);
        saveRow.add(status);
        c.gridy++;
        form.add(saveRow, c);
        add(form, BorderLayout.NORTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                openProfile();
            }
        });
        openProfile();
    }

    public void openProfile() {
        for (Map.Entry<String, String> setting : controller.getSettings().entrySet()) {
            fields.get(setting.getKey()).setText(setting.getValue());
        }
        status.setText(" ");
    }

    private void saveProfile() {
        for (Map.Entry<String, JTextField> field : fields.entrySet()) {
            editField(field.getKey(), field.getValue().getText().trim());
        }
        status.setText("Profile saved.");
    }

    private void editField(String name, String value) {
        controller.updateSetting(name, value);
    }
}
