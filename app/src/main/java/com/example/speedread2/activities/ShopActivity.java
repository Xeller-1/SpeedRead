package com.example.speedread2.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.speedread2.R;

/**
 * Activity для магазина
 * Отображает персонажа (синхрон с забегами) и товары в виде квадратиков
 * Позволяет покупать предметы за монетки
 */
public class ShopActivity extends AppCompatActivity {

    // Константы для работы с SharedPreferences
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_COINS = "coins";

    // Элементы интерфейса
    private ImageButton btnBack;
    private TextView tvCoins;
    private TextView tvCategoryClothing, tvCategoryPet, tvCategoryBackground;
    private TextView tvItem1, tvItem2, tvItem3, tvItem4;
    
    // Текущая выбранная категория
    private String currentCategory = "Одежда";

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // Инициализация элементов интерфейса
        btnBack = findViewById(R.id.btnBack);
        tvCoins = findViewById(R.id.tvCoins);
        tvCategoryClothing = findViewById(R.id.tvCategoryClothing);
        tvCategoryPet = findViewById(R.id.tvCategoryPet);
        tvCategoryBackground = findViewById(R.id.tvCategoryBackground);
        tvItem1 = findViewById(R.id.tvItem1);
        tvItem2 = findViewById(R.id.tvItem2);
        tvItem3 = findViewById(R.id.tvItem3);
        tvItem4 = findViewById(R.id.tvItem4);

        // Загружаем данные пользователя (количество монет)
        loadUserData();

        // Обработчик кнопки назад - возвращает на экран профиля
        btnBack.setOnClickListener(v -> finish());
        
        // Обработчики переключения категорий
        tvCategoryClothing.setOnClickListener(v -> switchCategory("Одежда"));
        tvCategoryPet.setOnClickListener(v -> switchCategory("Питомец"));
        tvCategoryBackground.setOnClickListener(v -> switchCategory("Фон"));
        
        // Устанавливаем начальную категорию
        switchCategory("Одежда");
    }

    /**
     * Загружает количество монет из SharedPreferences и отображает их
     */
    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int coins = prefs.getInt(KEY_COINS, 0);
        tvCoins.setText(String.valueOf(coins));
    }

    /**
     * Переключает категорию товаров и обновляет отображение
     * @param category - название категории: "Одежда", "Питомец" или "Фон"
     */
    private void switchCategory(String category) {
        currentCategory = category;
        
        // Сбрасываем стиль всех категорий
        tvCategoryClothing.setTextColor(getResources().getColor(R.color.black, null));
        tvCategoryPet.setTextColor(0xFF757575);
        tvCategoryBackground.setTextColor(0xFF757575);
        
        // Выделяем выбранную категорию синим цветом
        switch (category) {
            case "Одежда":
                tvCategoryClothing.setTextColor(getResources().getColor(R.color.primary_blue, null));
                // Обновляем названия товаров
                tvItem1.setText("Одежда 1");
                tvItem2.setText("Одежда 2");
                tvItem3.setText("Одежда 3");
                tvItem4.setText("Одежда 4");
                break;
            case "Питомец":
                tvCategoryPet.setTextColor(getResources().getColor(R.color.primary_blue, null));
                // Обновляем названия товаров
                tvItem1.setText("Питомец 1");
                tvItem2.setText("Питомец 2");
                tvItem3.setText("Питомец 3");
                tvItem4.setText("Питомец 4");
                break;
            case "Фон":
                tvCategoryBackground.setTextColor(getResources().getColor(R.color.primary_blue, null));
                // Обновляем названия товаров
                tvItem1.setText("Фон 1");
                tvItem2.setText("Фон 2");
                tvItem3.setText("Фон 3");
                tvItem4.setText("Фон 4");
                break;
        }
    }

    /**
     * Вызывается при возврате на этот экран
     * Обновляет количество монет на случай изменений
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем монетки при возврате на экран
        loadUserData();
    }
}
