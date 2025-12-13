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
 * Activity для отображения списка басен
 * Показывает карточки с баснями средней степени тяжести
 * Позволяет выбрать басню для чтения
 */
public class FablesActivity extends AppCompatActivity {

    private AppDatabase database;
    private TextDao textDao;
    private CategoryDao categoryDao;
    private List<Text> fables;

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fables);

        // Инициализация базы данных
        database = AppDatabase.getInstance(this);
        textDao = database.textDao();
        categoryDao = database.categoryDao();

        // Инициализация элементов интерфейса
        ImageButton btnBack = findViewById(R.id.btnBack);
        CardView cardFable1 = findViewById(R.id.cardFable1);
        CardView cardFable2 = findViewById(R.id.cardFable2);

        // Обработчик кнопки "Назад" - возвращает на предыдущий экран
        btnBack.setOnClickListener(v -> finish());

        // Загружаем басни из БД
        loadFables();

        // Настраиваем обработчики кликов на карточки
        if (fables != null && !fables.isEmpty()) {
            if (fables.size() > 0 && cardFable1 != null) {
                Text fable1 = fables.get(0);
                cardFable1.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ReadingActivity.class);
                    intent.putExtra("textId", fable1.id);
                    startActivity(intent);
                });
            }
            if (fables.size() > 1 && cardFable2 != null) {
                Text fable2 = fables.get(1);
                cardFable2.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ReadingActivity.class);
                    intent.putExtra("textId", fable2.id);
                    startActivity(intent);
                });
            }
        }
    }

    /**
     * Загружает басни из базы данных
     */
    private void loadFables() {
        Category fablesCategory = categoryDao.getCategoryByName("Басни");
        if (fablesCategory != null) {
            fables = textDao.getTextsByCategory(fablesCategory.id);
        }
    }
}
