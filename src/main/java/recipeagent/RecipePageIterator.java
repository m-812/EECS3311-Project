package recipeagent;

import java.util.ArrayList;
import java.util.List;

public class RecipePageIterator {
    private List<Recipe> recipes;
    private int pageSize;
    private int pageIndex = -1;

    public RecipePageIterator(List<Recipe> recipes, int pageSize) {
        this.recipes = new ArrayList<>(recipes);
        this.pageSize = pageSize;
    }

    public boolean hasNext() {
        return pageIndex == -1 || (pageIndex + 1) * pageSize < recipes.size();
    }

    public List<Recipe> next() {
        pageIndex++;
        return pageAt(pageIndex);
    }

    public boolean hasPrevious() {
        return pageIndex > 0;
    }

    public List<Recipe> previous() {
        pageIndex--;
        return pageAt(pageIndex);
    }

    private List<Recipe> pageAt(int index) {
        int from = index * pageSize;
        int to = Math.min(from + pageSize, recipes.size());
        return new ArrayList<>(recipes.subList(from, to));
    }
}
