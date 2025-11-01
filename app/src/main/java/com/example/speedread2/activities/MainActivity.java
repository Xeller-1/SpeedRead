package com.example.speedread2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.speedread2.R;

/**
 * Главная Activity приложения
 * Управляет навигацией между разделами: Забеги, Академия, Профиль
 * Использует FrameLayout для динамической загрузки контента
 */
public class MainActivity extends AppCompatActivity {

    // Константы для работы с SharedPreferences
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_COINS = "coins";
    
    // Кнопки нижней навигации
    private Button btnRaces, btnAcademy, btnProfile;
    // Контейнер для динамической загрузки контента
    private FrameLayout contentContainer;
    // Представление контента "Забеги" (начальный экран)
    private View racesContent;
    // Представление выбора категорий (стихи/рассказы)
    private View categorySelectionContent;

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация элементов интерфейса
        btnRaces = findViewById(R.id.btnRaces);
        btnAcademy = findViewById(R.id.btnAcademy);
        btnProfile = findViewById(R.id.btnProfile);
        contentContainer = findViewById(R.id.contentContainer);

        // По умолчанию показываем экран Забеги и подсвечиваем соответствующую кнопку
        showRacesContent();
        highlightButton(btnRaces);

        // Обработчик кнопки "Забеги" - показывает содержимое забегов
        btnRaces.setOnClickListener(v -> {
            showRacesContent();
            highlightButton(btnRaces);
        });

        // Обработчик кнопки "Академия" - открывает экран академии
        btnAcademy.setOnClickListener(v -> {
            Intent intent = new Intent(this, AcademyActivity.class);
            startActivity(intent);
            highlightButton(btnAcademy);
        });

        // Обработчик кнопки "Профиль" - открывает экран профиля
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            highlightButton(btnProfile);
        });
    }

    /**
     * Загружает и отображает начальный контент экрана "Забеги"
     * Показывает персонажа (круг) и кнопку "Играть"
     * Настраивает обработчики кликов для кнопки игры и монеток
     */
    private void showRacesContent() {
        // Очищаем контейнер перед загрузкой нового контента
        contentContainer.removeAllViews();
        
        // Загружаем layout для начального экрана Забеги
        LayoutInflater inflater = LayoutInflater.from(this);
        racesContent = inflater.inflate(R.layout.activity_races, contentContainer, false);
        contentContainer.addView(racesContent);

        // Инициализация элементов начального экрана
        Button btnPlay = racesContent.findViewById(R.id.btnPlay);
        TextView tvCoins = racesContent.findViewById(R.id.tvCoins);
        
        // Загружаем количество монет из SharedPreferences и отображаем
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int coins = prefs.getInt(KEY_COINS, 0);
        tvCoins.setText(String.valueOf(coins));

        // Обработчик клика на кнопку "Играть" - показывает выбор категорий
        btnPlay.setOnClickListener(v -> showCategorySelection());
    }

    /**
     * Загружает и отображает экран выбора категорий (Стихи/Рассказы)
     * Показывается после нажатия на кнопку "Играть"
     * Настраивает обработчики кликов для карточек категорий
     */
    private void showCategorySelection() {
        // Очищаем контейнер перед загрузкой нового контента
        contentContainer.removeAllViews();
        
        // Загружаем layout для выбора категорий
        LayoutInflater inflater = LayoutInflater.from(this);
        categorySelectionContent = inflater.inflate(R.layout.activity_races_category_selection, contentContainer, false);
        contentContainer.addView(categorySelectionContent);

        // Инициализация элементов экрана выбора категорий
        ImageButton btnBack = categorySelectionContent.findViewById(R.id.btnBack);
        CardView cardPoems = categorySelectionContent.findViewById(R.id.cardPoems);
        CardView cardFables = categorySelectionContent.findViewById(R.id.cardFables);
        CardView cardStories = categorySelectionContent.findViewById(R.id.cardStories);

        // Обработчик кнопки "Назад" - возвращает на начальный экран забегов
        btnBack.setOnClickListener(v -> showRacesContent());

        // Обработчик клика на карточку "Стихи" - открывает список стихотворений
        cardPoems.setOnClickListener(v -> {
            Intent intent = new Intent(this, PoemsActivity.class);
            startActivity(intent);
        });

        // Обработчик клика на карточку "Басни" - открывает список басен
        cardFables.setOnClickListener(v -> {
            Intent intent = new Intent(this, FablesActivity.class);
            startActivity(intent);
        });

        // Обработчик клика на карточку "Рассказы" - открывает список рассказов
        cardStories.setOnClickListener(v -> {
            Intent intent = new Intent(this, StoriesActivity.class);
            startActivity(intent);
        });
    }
    
    /**
     * Обновляет количество монет на экране при возврате из других Activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем монетки если на экране забегов
        if (racesContent != null && racesContent.getParent() != null) {
            TextView tvCoins = racesContent.findViewById(R.id.tvCoins);
            if (tvCoins != null) {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                int coins = prefs.getInt(KEY_COINS, 0);
                tvCoins.setText(String.valueOf(coins));
            }
        }
    }

    /**
     * Подсвечивает активную кнопку навигации
     * Сбрасывает все кнопки, затем выделяет выбранную
     * @param activeButton - кнопка, которую нужно подсветить
     */
    private void highlightButton(Button activeButton) {
        // Сбрасываем все кнопки до исходного состояния
        resetButton(btnRaces);
        resetButton(btnAcademy);
        resetButton(btnProfile);

        // Подсвечиваем активную кнопку синим цветом и полной непрозрачностью
        activeButton.setTextColor(getResources().getColor(R.color.primary_blue, null));
        activeButton.setAlpha(1.0f);
    }

    /**
     * Сбрасывает стиль кнопки до исходного состояния
     * Устанавливает черный цвет текста и уменьшает непрозрачность
     * @param button - кнопка, которую нужно сбросить
     */
    private void resetButton(Button button) {
        button.setTextColor(getResources().getColor(R.color.black, null));
        button.setAlpha(0.6f);
    }
}