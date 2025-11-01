package com.example.speedread2.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.speedread2.R;

/**
 * Activity для отображения списка стихотворений
 * Показывает карточки со стихотворениями разного уровня сложности
 * Позволяет выбрать стихотворение для чтения
 */
public class PoemsActivity extends AppCompatActivity {

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poems);

        // Инициализация элементов интерфейса
        ImageButton btnBack = findViewById(R.id.btnBack);
        CardView cardPoem1 = findViewById(R.id.cardPoem1);
        CardView cardPoem2 = findViewById(R.id.cardPoem2);
        CardView cardPoem3 = findViewById(R.id.cardPoem3);

        // Обработчик кнопки "Назад" - возвращает на предыдущий экран
        btnBack.setOnClickListener(v -> finish());

        // Обработчик клика на карточку стихотворения 1
        cardPoem1.setOnClickListener(v -> {
            Toast.makeText(this, "Стихотворение 1", Toast.LENGTH_SHORT).show();
            // TODO: Здесь будет открытие чтения стихотворения
        });

        // Обработчик клика на карточку стихотворения 2
        cardPoem2.setOnClickListener(v -> {
            Toast.makeText(this, "Стихотворение 2", Toast.LENGTH_SHORT).show();
            // TODO: Здесь будет открытие чтения стихотворения
        });

        // Обработчик клика на карточку стихотворения 3
        cardPoem3.setOnClickListener(v -> {
            Toast.makeText(this, "Стихотворение 3", Toast.LENGTH_SHORT).show();
            // TODO: Здесь будет открытие чтения стихотворения
        });
    }
}
