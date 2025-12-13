package com.example.speedread2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.speedread2.R;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.database.DatabaseInitializer;

/**
 * Splash Screen Activity - начальный экран приложения
 * Показывает название "SpeedRead" в течение 6 секунд,
 * затем проверяет статус входа и перенаправляет пользователя
 */
public class SplashActivity extends AppCompatActivity {

    // Длительность показа splash screen (6 секунд)
    private static final int SPLASH_DURATION = 6000;

    /**
     * Вызывается при создании Activity
     * Устанавливает разметку и запускает таймер для перехода на следующий экран
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Инициализируем базу данных синхронно с обработкой ошибок
        try {
            Log.d("SplashActivity", "Инициализация БД...");
            AppDatabase.getInstance(this);
            Log.d("SplashActivity", "БД создана, инициализация данных...");
            DatabaseInitializer.initializeDatabase(this);
            Log.d("SplashActivity", "Инициализация завершена");
        } catch (Exception e) {
            Log.e("SplashActivity", "Ошибка инициализации БД", e);
            e.printStackTrace();
            // В случае ошибки продолжаем работу
        }

        // Запускаем таймер на 6 секунд для показа splash screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Получаем доступ к сохраненным данным
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            // Проверяем, был ли пользователь ранее авторизован
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

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
