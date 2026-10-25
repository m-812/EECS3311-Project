package recipeagent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PantryManager {
    private static PantryManager instance;
    private Map<String, PantryItem> items = new LinkedHashMap<>();
    public static final int MAX_NAME_LENGTH = 50;

    private PantryManager() {
    }

    public static PantryManager getInstance() {
        if (instance == null) {
            instance = new PantryManager();
        }
        return instance;
    }

    public boolean addItem(PantryItem item) {
        if (item.getName().isBlank() || item.getQuantity().isBlank()
                || item.getName().length() > MAX_NAME_LENGTH || nameExists(item.getName())) {
            return false;
        }
        items.put(item.getName().toLowerCase(), item);
        return true;
    }

    public PantryItem getItem(String id) {
        return items.get(id.toLowerCase());
    }

    public boolean removeItem(String id) {
        return items.remove(id.toLowerCase()) != null;
    }

    public boolean updateItem(String id, String quantity) {
        PantryItem item = getItem(id);
        if (item == null || quantity.isBlank()) {
            return false;
        }
        item.setQuantity(quantity);
        return true;
    }

    public boolean nameExists(String name) {
        return items.containsKey(name.toLowerCase());
    }

    public List<PantryItem> getItems() {
        return new ArrayList<>(items.values());
    }
}
