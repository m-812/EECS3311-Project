package recipeagent;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProfileManager {
    private static ProfileManager instance;
    private Map<String, String> settings = new LinkedHashMap<>();

    private ProfileManager() {
        settings.put("Dietary restrictions", "");
        settings.put("Preferred foods", "");
        settings.put("Disliked ingredients", "");
        settings.put("Nutrition goals", "");
        settings.put("Other preferences", "");
    }

    public static ProfileManager getInstance() {
        if (instance == null) {
            instance = new ProfileManager();
        }
        return instance;
    }

    public String getSetting(String name) {
        return settings.get(name);
    }

    public boolean setSetting(String name, String value) {
        if (!settings.containsKey(name)) {
            return false;
        }
        settings.put(name, value);
        return true;
    }

    public void clearSetting(String name) {
        settings.replace(name, "");
    }

    public Map<String, String> getSettings() {
        return new LinkedHashMap<>(settings);
    }
}
