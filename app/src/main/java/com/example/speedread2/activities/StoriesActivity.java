package com.example.speedread2.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import android.content.Intent;
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

        // Настраиваем обработчик клика на карточку
        if (stories != null && !stories.isEmpty() && cardStory1 != null) {
            Text story1 = stories.get(0);
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
}
