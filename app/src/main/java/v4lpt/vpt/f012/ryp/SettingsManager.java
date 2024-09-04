package v4lpt.vpt.f012.ryp;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREFS_NAME = "ImageRatingPrefs";
    private static final String KEY_SHOW_RATING_OVERLAY = "show_rating_overlay";

    private SharedPreferences sharedPreferences;

    public SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isShowRatingOverlay() {
        return sharedPreferences.getBoolean(KEY_SHOW_RATING_OVERLAY, false);
    }

    public void setShowRatingOverlay(boolean show) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SHOW_RATING_OVERLAY, show);
        editor.apply();
    }
}