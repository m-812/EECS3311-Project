package recipeagent;

import java.util.Map;

public class ProfileController {
    private ProfileManager profileManager;

    public ProfileController() {
        profileManager = ProfileManager.getInstance();
    }

    public Map<String, String> getSettings() {
        return profileManager.getSettings();
    }

    public boolean updateSetting(String name, String value) {
        return profileManager.setSetting(name, value);
    }
}
