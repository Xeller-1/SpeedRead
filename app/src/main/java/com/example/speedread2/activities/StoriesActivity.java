package com.example.speedread2.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.speedread2.R;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.dao.CategoryDao;
import com.example.speedread2.dao.TextDao;
import com.example.speedread2.database.entities.Category;
import com.example.speedread2.database.entities.Text;

import java.util.List;

/**
 * Activity для отображения списка рассказов
 * Показывает карточки с рассказами разного уровня сложности
 * Позволяет выбрать рассказ для чтения
 */
public class StoriesActivity extends AppCompatActivity {

    private AppDatabase database;
    private TextDao textDao;
    private CategoryDao categoryDao;
    private List<Text> stories;

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        applyBackground();

        // Инициализация базы данных
        database = AppDatabase.getInstance(this);
        textDao = database.textDao();
        categoryDao = database.categoryDao();

        // Инициализация элементов интерфейса
        ImageButton btnBack = findViewById(R.id.btnBack);
        CardView cardStory1 = findViewById(R.id.cardStory1);

        // Обработчик кнопки "Назад" - возвращает на предыдущий экран
        btnBack.setOnClickListener(v -> finish());

        // Загружаем рассказы из БД
        loadStories();

        // Настраиваем обработчик клика на карточку и отображаем данные
        if (stories != null && !stories.isEmpty() && cardStory1 != null) {
            Text story1 = stories.get(0);
            
            // Находим TextViews в карточке
            android.widget.TextView textTitle = cardStory1.findViewById(R.id.textTitle1);
            android.widget.TextView textAuthor = cardStory1.findViewById(R.id.textAuthor1);
            
            if (textTitle != null && story1.title != null) {
                textTitle.setText(story1.title);
            }
            if (textAuthor != null && story1.author != null) {
                textAuthor.setText(story1.author);
            }
            
        cardStory1.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReadingActivity.class);
                intent.putExtra("textId", story1.id);
                startActivity(intent);
            });
        }
    }

    /**
     * Загружает рассказы из базы данных
     */
    private void loadStories() {
        Category storiesCategory = categoryDao.getCategoryByName("Рассказы");
        if (storiesCategory != null) {
            stories = textDao.getTextsByCategory(storiesCategory.id);
        }
    }

    private void applyBackground() {
        View rootView = findViewById(android.R.id.content).getRootView();

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String backgroundName = prefs.getString("selectedBackground", null);

        if (backgroundName != null && backgroundName.equals("Звездный фон")) {
            rootView.setBackgroundResource(R.drawable.splash_background);
            return;
        }

        int backgroundColor;
        if (backgroundName != null) {
            backgroundColor = getBackgroundColor(backgroundName);
        } else {
            backgroundColor = 0xFFFFFFFF;
        }

        rootView.setBackgroundColor(backgroundColor);
    }

    private int getBackgroundColor(String backgroundName) {
        switch (backgroundName) {
            case "Синий фон":
                return 0xFF2196F3;
            case "Звездный фон":
                return 0xFF0a0e27;
            case "Красный фон":
                return 0xFFF44336;
            case "Фиолетовый фон":
                return 0xFF9C27B0;
            default:
                return 0xFFFFFFFF;
        }
    }

}
