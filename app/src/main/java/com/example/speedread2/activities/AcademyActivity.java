package com.example.speedread2.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.speedread2.R;

/**
 * Activity для раздела "Академия"
 * Показывает список доступных уроков по технике скорочтения
 * Позволяет выбрать урок для изучения
 */
public class AcademyActivity extends AppCompatActivity {

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academy);

        // Инициализация элементов интерфейса
        ImageButton btnBack = findViewById(R.id.btnBack);
        CardView cardLesson1 = findViewById(R.id.cardLesson1);
        CardView cardLesson2 = findViewById(R.id.cardLesson2);
        CardView cardLesson3 = findViewById(R.id.cardLesson3);

        // Обработчик кнопки "Назад" - возвращает на предыдущий экран
        btnBack.setOnClickListener(v -> finish());

        // Обработчики кликов на карточки уроков
        cardLesson1.setOnClickListener(v -> {
            Toast.makeText(this, "Четко произносим звуки", Toast.LENGTH_SHORT).show();
            // TODO: Здесь будет открытие урока
        });

        cardLesson2.setOnClickListener(v -> {
            Toast.makeText(this, "Читаем выразительно", Toast.LENGTH_SHORT).show();
            // TODO: Здесь будет открытие урока
        });

        cardLesson3.setOnClickListener(v -> {
            Toast.makeText(this, "Управляем скоростью", Toast.LENGTH_SHORT).show();
            // TODO: Здесь будет открытие урока
        });
    }
}
