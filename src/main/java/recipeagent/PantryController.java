package recipeagent;

import java.util.List;

public class PantryController {
    private PantryManager pantryManager;

    public PantryController() {
        pantryManager = PantryManager.getInstance();
    }

    public List<PantryItem> getItems() {
        return pantryManager.getItems();
    }

    public boolean addItem(String name, String quantity) {
        return pantryManager.addItem(new PantryItem(name, quantity));
    }

    public boolean updateItem(String name, String quantity) {
        return pantryManager.updateItem(name, quantity);
    }

    public boolean removeItem(String name) {
        return pantryManager.removeItem(name);
    }
}
