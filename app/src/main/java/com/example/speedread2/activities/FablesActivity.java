package com.example.speedread2.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.speedread2.R;

/**
 * Activity для отображения списка басен
 * Показывает карточки с баснями средней степени тяжести
 * Позволяет выбрать басню для чтения
 */
public class FablesActivity extends AppCompatActivity {

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fables);

        // Инициализация элементов интерфейса
        ImageButton btnBack = findViewById(R.id.btnBack);
        CardView cardFable1 = findViewById(R.id.cardFable1);
        CardView cardFable2 = findViewById(R.id.cardFable2);

        // Обработчик кнопки "Назад" - возвращает на предыдущий экран
        btnBack.setOnClickListener(v -> finish());

        // Обработчик клика на карточку басни 1
        cardFable1.setOnClickListener(v -> {
            Toast.makeText(this, "Басня 1", Toast.LENGTH_SHORT).show();
            // TODO: Здесь будет открытие чтения басни
        });

        // Обработчик клика на карточку басни 2
        cardFable2.setOnClickListener(v -> {
            Toast.makeText(this, "Басня 2", Toast.LENGTH_SHORT).show();
            // TODO: Здесь будет открытие чтения басни
        });
    }
}
