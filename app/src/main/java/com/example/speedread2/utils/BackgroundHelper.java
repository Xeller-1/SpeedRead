package com.example.speedread2.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.example.speedread2.R;

public final class BackgroundHelper {
    private BackgroundHelper() {
    }

    public static void applyBackground(Activity activity) {
        if (activity == null) return;
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) return;
        applyBackgroundToView(activity, rootView);
    }

    public static void applyBackgroundToView(Context context, View view) {
        if (context == null || view == null) return;

        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String backgroundName = prefs.getString("selectedBackground", null);

        Integer drawableRes = getBackgroundDrawable(backgroundName);
        if (drawableRes != null) {
            view.setBackgroundResource(drawableRes);
        } else {
            view.setBackgroundColor(0xFFFFFFFF);
        }
    }

    public static Integer getBackgroundDrawable(String backgroundName) {
        if (backgroundName == null) return null;

        switch (backgroundName) {
            case "Синий фон":
                return R.drawable.background_blue;
            case "Звездный фон":
                return R.drawable.splash_background;
            case "Красный фон":
                return R.drawable.background_red;
            case "Фиолетовый фон":
                return R.drawable.background_purple;
            default:
                return null;
        }
    }
}
