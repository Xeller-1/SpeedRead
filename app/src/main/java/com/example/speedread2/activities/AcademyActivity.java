package com.example.speedread2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.speedread2.R;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.dao.TongueTwisterDao;
import com.example.speedread2.database.entities.TongueTwister;

import java.util.List;

/**
 * Activity для раздела "Академия"
 * Показывает список скороговорок из базы данных
 * Позволяет выбрать скороговорку для чтения
 */
public class AcademyActivity extends AppCompatActivity {

    private AppDatabase database;
    private TongueTwisterDao tongueTwisterDao;
    private LinearLayout containerTongueTwisters;

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academy);

        // Инициализация базы данных
        database = AppDatabase.getInstance(this);
        tongueTwisterDao = database.tongueTwisterDao();

        // Применяем фон
        applyBackground();

        // Инициализация элементов интерфейса
        ImageButton btnBack = findViewById(R.id.btnBack);
        containerTongueTwisters = findViewById(R.id.containerTongueTwisters);

        // Обработчик кнопки "Назад" - возвращает на предыдущий экран
        btnBack.setOnClickListener(v -> finish());

        // Загружаем и отображаем скороговорки
        loadTongueTwisters();
    }
    
    /**
     * Применяет выбранный фон из настроек (по умолчанию белый)
     */
    private void applyBackground() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String backgroundName = prefs.getString("selectedBackground", null);
        
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            // Для звездного фона используем drawable ресурс
            if (backgroundName != null && backgroundName.equals("Звездный фон")) {
                rootView.setBackgroundResource(R.drawable.splash_background);
                return;
            }
            
            int backgroundColor;
            if (backgroundName != null) {
                backgroundColor = getBackgroundColor(backgroundName);
            } else {
                backgroundColor = 0xFFFFFFFF; // Белый по умолчанию
            }
            
            rootView.setBackgroundColor(backgroundColor);
        }
    }
    
    /**
     * Возвращает цвет фона по имени
     */
    private int getBackgroundColor(String backgroundName) {
        switch (backgroundName) {
            case "Синий фон":
                return 0xFF2196F3; // Синий
            case "Звездный фон":
                return 0xFF0a0e27; // Темно-синий для звездного фона (fallback)
            case "Красный фон":
                return 0xFFF44336; // Красный
            case "Фиолетовый фон":
                return 0xFF9C27B0; // Фиолетовый
            default:
                return 0xFFFFFFFF; // Белый по умолчанию
        }
    }

    /**
     * Загружает скороговорки из базы данных и создает карточки
     */
    private void loadTongueTwisters() {
        List<TongueTwister> tongueTwisters = tongueTwisterDao.getAllTongueTwisters();
        
        if (tongueTwisters.isEmpty()) {
            // Если скороговорок нет, показываем сообщение
            TextView emptyText = new TextView(this);
            emptyText.setText("Скороговорки загружаются...");
            emptyText.setTextSize(16f);
            emptyText.setPadding(16, 32, 16, 16);
            containerTongueTwisters.addView(emptyText);
            return;
        }

        // Создаем карточку для каждой скороговорки
        LayoutInflater inflater = LayoutInflater.from(this);
        for (TongueTwister tongueTwister : tongueTwisters) {
            View cardView = inflater.inflate(R.layout.item_tongue_twister, containerTongueTwisters, false);
            
            TextView tvTitle = cardView.findViewById(R.id.tvTitle);
            TextView tvContent = cardView.findViewById(R.id.tvContent);
            TextView tvSounds = cardView.findViewById(R.id.tvSounds);
            TextView tvDifficulty = cardView.findViewById(R.id.tvDifficulty);
            CardView card = (CardView) cardView;
            
            tvTitle.setText(tongueTwister.title);
            tvContent.setText(tongueTwister.content);
            tvSounds.setText("Звуки: " + tongueTwister.sounds);
            
            // Устанавливаем уровень сложности
            String difficultyText;
            switch (tongueTwister.difficulty) {
                case 1:
                    difficultyText = "Легко";
                    break;
                case 2:
                    difficultyText = "Средне";
                    break;
                case 3:
                    difficultyText = "Сложно";
                    break;
                default:
                    difficultyText = "Уровень " + tongueTwister.difficulty;
            }
            tvDifficulty.setText(difficultyText);
            
            // Обработчик клика - открываем чтение скороговорки
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, TongueTwisterActivity.class);
                intent.putExtra("tongueTwisterId", tongueTwister.id);
                startActivity(intent);
            });
            
            containerTongueTwisters.addView(cardView);
        }
    }
}
