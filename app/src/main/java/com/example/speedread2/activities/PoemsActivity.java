package com.example.speedread2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

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
 * Activity для отображения списка стихотворений
 * Показывает карточки со стихотворениями разного уровня сложности
 * Позволяет выбрать стихотворение для чтения
 */
public class PoemsActivity extends AppCompatActivity {

    private AppDatabase database;
    private TextDao textDao;
    private CategoryDao categoryDao;
    private List<Text> poems;

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poems);

        // Инициализация базы данных
        database = AppDatabase.getInstance(this);
        textDao = database.textDao();
        categoryDao = database.categoryDao();

        // Инициализация элементов интерфейса
        ImageButton btnBack = findViewById(R.id.btnBack);
        CardView cardPoem1 = findViewById(R.id.cardPoem1);
        CardView cardPoem2 = findViewById(R.id.cardPoem2);
        CardView cardPoem3 = findViewById(R.id.cardPoem3);

        // Обработчик кнопки "Назад" - возвращает на предыдущий экран
        btnBack.setOnClickListener(v -> finish());

        // Загружаем стихи из БД
        loadPoems();

        // Настраиваем обработчики кликов на карточки и обновляем тексты
        if (poems != null && !poems.isEmpty()) {
            if (poems.size() > 0 && cardPoem1 != null) {
                Text poem1 = poems.get(0);
                android.widget.TextView title1 = cardPoem1.findViewById(R.id.textTitle1);
                android.widget.TextView author1 = cardPoem1.findViewById(R.id.textAuthor1);
                if (title1 != null) title1.setText(poem1.title);
                if (author1 != null) author1.setText(poem1.author);
                cardPoem1.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ReadingActivity.class);
                    intent.putExtra("textId", poem1.id);
                    startActivity(intent);
                });
            }
            if (poems.size() > 1 && cardPoem2 != null) {
                Text poem2 = poems.get(1);
                android.widget.TextView title2 = cardPoem2.findViewById(R.id.textTitle2);
                android.widget.TextView author2 = cardPoem2.findViewById(R.id.textAuthor2);
                if (title2 != null) title2.setText(poem2.title);
                if (author2 != null) author2.setText(poem2.author);
                cardPoem2.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ReadingActivity.class);
                    intent.putExtra("textId", poem2.id);
                    startActivity(intent);
                });
            }
            if (poems.size() > 2 && cardPoem3 != null) {
                Text poem3 = poems.get(2);
                android.widget.TextView title3 = cardPoem3.findViewById(R.id.textTitle3);
                android.widget.TextView author3 = cardPoem3.findViewById(R.id.textAuthor3);
                if (title3 != null) title3.setText(poem3.title);
                if (author3 != null) author3.setText(poem3.author);
                cardPoem3.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ReadingActivity.class);
                    intent.putExtra("textId", poem3.id);
                    startActivity(intent);
                });
            }
        }
    }

    /**
     * Загружает стихи из базы данных
     */
    private void loadPoems() {
        Category poemsCategory = categoryDao.getCategoryByName("Стихи");
        if (poemsCategory != null) {
            poems = textDao.getTextsByCategory(poemsCategory.id);
        }
    }
}
