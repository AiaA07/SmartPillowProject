package com.example.smartpillow;

import android.content.Context;
import android.content.SharedPreferences;

public class UserProfileManager {

    private static final String PREF_NAME = "user_profile";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ---- Sleep goal ----
    public static boolean hasSleepGoal(Context context) {
        return getPrefs(context).contains("sleepGoalHours");
    }

    public static int getSleepGoalHours(Context context) {
        return getPrefs(context).getInt("sleepGoalHours", 8); // default 8
    }

    public static void setSleepGoalHours(Context context, int hours) {
        getPrefs(context).edit().putInt("sleepGoalHours", hours).apply();
    }

    // (You can add getters for bedtime, wakeTime, etc. later if needed)
}
