package recipeagent;

public class PantryItem {
    private String name;
    private String quantity;

    public PantryItem(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public PantryItem(PantryItem other) {
        this.name = other.name;
        this.quantity = other.quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
