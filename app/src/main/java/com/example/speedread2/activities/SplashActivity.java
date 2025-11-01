package com.example.speedread2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.speedread2.R;

/**
 * Splash Screen Activity - начальный экран приложения
 * Показывает название "SpeedRead" в течение 6 секунд,
 * затем проверяет статус входа и перенаправляет пользователя
 */
public class SplashActivity extends AppCompatActivity {

    // Длительность показа splash screen (6 секунд)
    private static final int SPLASH_DURATION = 6000;
    // Имя файла SharedPreferences для хранения данных пользователя
    private static final String PREFS_NAME = "UserPrefs";
    // Ключ для сохранения статуса входа пользователя
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    /**
     * Вызывается при создании Activity
     * Устанавливает разметку и запускает таймер для перехода на следующий экран
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Запускаем таймер на 6 секунд для показа splash screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Получаем доступ к сохраненным данным
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            // Проверяем, был ли пользователь ранее авторизован
            boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

            Intent intent;
            if (isLoggedIn) {
                // Если пользователь уже заходил, переходим к главному экрану
                intent = new Intent(this, MainActivity.class);
            } else {
                // Иначе показываем экран входа для нового пользователя
                intent = new Intent(this, LoginActivity.class);
            }
            
            // Запускаем нужный экран и закрываем splash screen
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
