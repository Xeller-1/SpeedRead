package com.example.speedread2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.speedread2.R;
import com.example.speedread2.utils.BackgroundHelper;
import com.example.speedread2.dao.TongueTwisterDao;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.database.entities.TongueTwister;

import java.util.List;

/**
 * Activity для отображения раздела "Академия"
 * Показывает список скороговорок из базы данных
 * Позволяет выбрать скороговорку для чтения
 */
public class AcademyActivity extends AppCompatActivity {

    private AppDatabase database;
    private TongueTwisterDao tongueTwisterDao;
    private LinearLayout containerTongueTwisters;
    private Integer selectedDifficulty = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academy);

        database = AppDatabase.getInstance(this);
        tongueTwisterDao = database.tongueTwisterDao();

        BackgroundHelper.applyBackground(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnFilter = findViewById(R.id.btnFilter);
        containerTongueTwisters = findViewById(R.id.containerTongueTwisters);

        btnBack.setOnClickListener(v -> finish());
        btnFilter.setOnClickListener(this::showFilterMenu);

        loadTongueTwisters();
    }

    private void showFilterMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenu().add(Menu.NONE, 1, Menu.NONE, "Лёгкие");
        popupMenu.getMenu().add(Menu.NONE, 2, Menu.NONE, "Средние");
        popupMenu.getMenu().add(Menu.NONE, 3, Menu.NONE, "Сложные");
        popupMenu.getMenu().add(Menu.NONE, 4, Menu.NONE, "Сбросить фильтр");

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    selectedDifficulty = 1;
                    break;
                case 2:
                    selectedDifficulty = 2;
                    break;
                case 3:
                    selectedDifficulty = 3;
                    break;
                case 4:
                default:
                    selectedDifficulty = null;
                    break;
            }
            loadTongueTwisters();
            return true;
        });

        popupMenu.show();
    }



    /**
     * Загружает скороговорки из базы данных и создает карточки
     */
    private void loadTongueTwisters() {
        List<TongueTwister> tongueTwisters;
        if (selectedDifficulty == null) {
            tongueTwisters = tongueTwisterDao.getAllTongueTwisters();
        } else {
            tongueTwisters = tongueTwisterDao.getTongueTwistersByDifficulty(selectedDifficulty);
        }

        containerTongueTwisters.removeAllViews();

        if (tongueTwisters.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Нет скороговорок для выбранного фильтра");
            emptyText.setTextSize(16f);
            emptyText.setPadding(16, 32, 16, 16);
            containerTongueTwisters.addView(emptyText);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (TongueTwister tongueTwister : tongueTwisters) {
            View cardView = inflater.inflate(R.layout.item_tongue_twister, containerTongueTwisters, false);

            TextView tvTitle = cardView.findViewById(R.id.tvTitle);
            TextView tvContent = cardView.findViewById(R.id.tvContent);
            TextView tvSounds = cardView.findViewById(R.id.tvSounds);
            TextView tvDifficulty = cardView.findViewById(R.id.tvDifficulty);
            CardView card = (CardView) cardView;

            tvTitle.setText(tongueTwister.title);
            tvContent.setText(tongueTwister.content);
            tvSounds.setText("Звуки: " + tongueTwister.sounds);

            String difficultyText;
            switch (tongueTwister.difficulty) {
                case 1:
                    difficultyText = "Легко";
                    break;
                case 2:
                    difficultyText = "Средне";
                    break;
                case 3:
                    difficultyText = "Сложно";
                    break;
                default:
                    difficultyText = "Уровень " + tongueTwister.difficulty;
            }
            tvDifficulty.setText(difficultyText);

            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, TongueTwisterActivity.class);
                intent.putExtra("tongueTwisterId", tongueTwister.id);
                startActivity(intent);
            });

            containerTongueTwisters.addView(cardView);
        }
    }
}
