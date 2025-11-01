package com.example.speedread2.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.speedread2.R;

/**
 * Activity для отображения списка рассказов
 * Показывает карточки с рассказами разного уровня сложности
 * Позволяет выбрать рассказ для чтения
 */
public class StoriesActivity extends AppCompatActivity {

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        // Инициализация элементов интерфейса
        ImageButton btnBack = findViewById(R.id.btnBack);
        CardView cardStory1 = findViewById(R.id.cardStory1);

        // Обработчик кнопки "Назад" - возвращает на предыдущий экран
        btnBack.setOnClickListener(v -> finish());

        // Обработчик клика на карточку рассказа
        cardStory1.setOnClickListener(v -> {
            Toast.makeText(this, "Рассказ 1", Toast.LENGTH_SHORT).show();
            // TODO: Здесь будет открытие чтения рассказа
        });
    }
}
