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
import com.example.speedread2.dao.TongueTwisterDao;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.database.entities.TongueTwister;
import com.example.speedread2.utils.BackgroundHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AcademyActivity extends AppCompatActivity {

    private static final int SORT_DEFAULT = 0;
    private static final int SORT_TITLE_ASC = 1;
    private static final int SORT_DIFFICULTY_ASC = 2;
    private static final int SORT_DIFFICULTY_DESC = 3;

    private TongueTwisterDao tongueTwisterDao;
    private LinearLayout containerTongueTwisters;
    private TextView tvFilterStatus;

    private TextView chipAll;
    private TextView chipEasy;
    private TextView chipMedium;
    private TextView chipHard;

    private Integer selectedDifficulty = null;
    private int selectedSort = SORT_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academy);

        AppDatabase database = AppDatabase.getInstance(this);
        tongueTwisterDao = database.tongueTwisterDao();

        BackgroundHelper.applyBackground(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnSort = findViewById(R.id.btnSort);
        tvFilterStatus = findViewById(R.id.tvFilterStatus);
        containerTongueTwisters = findViewById(R.id.containerTongueTwisters);

        chipAll = findViewById(R.id.chipAll);
        chipEasy = findViewById(R.id.chipEasy);
        chipMedium = findViewById(R.id.chipMedium);
        chipHard = findViewById(R.id.chipHard);

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnSort.setOnClickListener(this::showSortMenu);

        chipAll.setOnClickListener(v -> selectDifficulty(null));
        chipEasy.setOnClickListener(v -> selectDifficulty(1));
        chipMedium.setOnClickListener(v -> selectDifficulty(2));
        chipHard.setOnClickListener(v -> selectDifficulty(3));

        updateDifficultyChips();
        loadTongueTwisters();
    }

    private void selectDifficulty(Integer difficulty) {
        selectedDifficulty = difficulty;
        updateDifficultyChips();
        loadTongueTwisters();
    }

    private void updateDifficultyChips() {
        setChipState(chipAll, selectedDifficulty == null);
        setChipState(chipEasy, selectedDifficulty != null && selectedDifficulty == 1);
        setChipState(chipMedium, selectedDifficulty != null && selectedDifficulty == 2);
        setChipState(chipHard, selectedDifficulty != null && selectedDifficulty == 3);
    }

    private void setChipState(TextView chip, boolean active) {
        chip.setBackgroundResource(active ? R.drawable.bg_chip_filter_active : R.drawable.bg_chip_filter);
        chip.setTextColor(getColor(active ? R.color.primary : R.color.text_secondary));
    }

    private void showSortMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenu().add(Menu.NONE, 1, Menu.NONE, "По умолчанию");
        popupMenu.getMenu().add(Menu.NONE, 2, Menu.NONE, "По названию А-Я");
        popupMenu.getMenu().add(Menu.NONE, 3, Menu.NONE, "Сложность: легко → сложно");
        popupMenu.getMenu().add(Menu.NONE, 4, Menu.NONE, "Сложность: сложно → легко");

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 2:
                    selectedSort = SORT_TITLE_ASC;
                    break;
                case 3:
                    selectedSort = SORT_DIFFICULTY_ASC;
                    break;
                case 4:
                    selectedSort = SORT_DIFFICULTY_DESC;
                    break;
                case 1:
                default:
                    selectedSort = SORT_DEFAULT;
                    break;
            }
            loadTongueTwisters();
            return true;
        });

        popupMenu.show();
    }

    private void loadTongueTwisters() {
        List<TongueTwister> source = tongueTwisterDao.getAllTongueTwisters();
        List<TongueTwister> tongueTwisters = new ArrayList<>();

        for (TongueTwister item : source) {
            if (selectedDifficulty == null || item.difficulty == selectedDifficulty) {
                tongueTwisters.add(item);
            }
        }

        applySort(tongueTwisters);
        updateFilterStatus();

        containerTongueTwisters.removeAllViews();

        if (tongueTwisters.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Нет скороговорок для выбранного фильтра");
            emptyText.setTextSize(15f);
            emptyText.setPadding(16, 32, 16, 16);
            emptyText.setTextColor(getColor(R.color.text_secondary));
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

            bindDifficultyChip(tvDifficulty, tongueTwister.difficulty);

            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, TongueTwisterActivity.class);
                intent.putExtra("tongueTwisterId", tongueTwister.id);
                startActivity(intent);
            });

            containerTongueTwisters.addView(cardView);
        }
    }

    private void bindDifficultyChip(TextView tvDifficulty, int difficulty) {
        switch (difficulty) {
            case 1:
                tvDifficulty.setText("Легко");
                tvDifficulty.setTextColor(getColor(R.color.accent_success));
                tvDifficulty.setBackgroundResource(R.drawable.bg_chip_easy);
                break;
            case 2:
                tvDifficulty.setText("Средне");
                tvDifficulty.setTextColor(getColor(R.color.accent_warning));
                tvDifficulty.setBackgroundResource(R.drawable.bg_chip_medium);
                break;
            case 3:
                tvDifficulty.setText("Сложно");
                tvDifficulty.setTextColor(getColor(R.color.primary_dark));
                tvDifficulty.setBackgroundResource(R.drawable.bg_chip_hard);
                break;
            default:
                tvDifficulty.setText("Уровень " + difficulty);
                tvDifficulty.setTextColor(getColor(R.color.text_secondary));
                tvDifficulty.setBackgroundResource(R.drawable.bg_chip_filter);
                break;
        }
    }

    private void applySort(List<TongueTwister> tongueTwisters) {
        switch (selectedSort) {
            case SORT_TITLE_ASC:
                tongueTwisters.sort(Comparator.comparing(t -> t.title == null ? "" : t.title));
                break;
            case SORT_DIFFICULTY_ASC:
                tongueTwisters.sort(Comparator.comparingInt(t -> t.difficulty));
                break;
            case SORT_DIFFICULTY_DESC:
                tongueTwisters.sort((a, b) -> Integer.compare(b.difficulty, a.difficulty));
                break;
            case SORT_DEFAULT:
            default:
                break;
        }
    }

    private void updateFilterStatus() {
        if (tvFilterStatus == null) return;

        String difficulty = selectedDifficulty == null
            ? "все"
            : (selectedDifficulty == 1 ? "лёгкие" : selectedDifficulty == 2 ? "средние" : "сложные");

        String sort;
        switch (selectedSort) {
            case SORT_TITLE_ASC:
                sort = "название А-Я";
                break;
            case SORT_DIFFICULTY_ASC:
                sort = "сложность ↑";
                break;
            case SORT_DIFFICULTY_DESC:
                sort = "сложность ↓";
                break;
            case SORT_DEFAULT:
            default:
                sort = "по умолчанию";
                break;
        }

        tvFilterStatus.setText("Фильтр: " + difficulty + " • Сортировка: " + sort);
    }
}
