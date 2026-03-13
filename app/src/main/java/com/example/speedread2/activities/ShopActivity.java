package com.example.speedread2.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.speedread2.R;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.dao.ShopItemDao;
import com.example.speedread2.dao.UserDao;
import com.example.speedread2.database.entities.ShopItem;
import com.example.speedread2.database.entities.User;
import com.example.speedread2.utils.BackgroundHelper;

import java.util.List;

/**
 * Activity для магазина
 * Отображает персонажа (синхрон с забегами) и товары в виде квадратиков
 * Позволяет покупать предметы за монетки
 */
public class ShopActivity extends AppCompatActivity {

    // Элементы интерфейса
    private ImageButton btnBack;
    private TextView tvCoins;
    private TextView tvCategoryBackground;
    private TextView tvItem1, tvItem2, tvItem3, tvItem4;
    
    // База данных
    private AppDatabase database;
    private ShopItemDao shopItemDao;
    private UserDao userDao;
    private int currentUserId;
    
    // Текущая выбранная категория
    private String currentCategory = "Фон";
    private List<ShopItem> currentItems;

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // Инициализация базы данных
        database = AppDatabase.getInstance(this);
        shopItemDao = database.shopItemDao();
        userDao = database.userDao();
        
        // Получаем ID текущего пользователя
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("currentUserId", -1);

        // Инициализация элементов интерфейса
        btnBack = findViewById(R.id.btnBack);
        tvCoins = findViewById(R.id.tvCoins);
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
        tvCategoryBackground.setOnClickListener(v -> switchCategory("Фон"));
        
        // Устанавливаем начальную категорию
        switchCategory("Фон");
        
        // Обработчики покупки товаров
        setupItemClickListeners();
    }

    /**
     * Загружает количество монет из БД и отображает их
     */
    private void loadUserData() {
        if (currentUserId != -1) {
            User user = userDao.getUserById(currentUserId);
            if (user != null) {
                tvCoins.setText(String.valueOf(user.coins));
            } else {
                tvCoins.setText("0");
            }
        } else {
            tvCoins.setText("0");
        }
    }
    
    /**
     * Настраивает обработчики кликов на товары
     */
    private void setupItemClickListeners() {
        tvItem1.setOnClickListener(v -> purchaseItem(0));
        tvItem2.setOnClickListener(v -> purchaseItem(1));
        tvItem3.setOnClickListener(v -> purchaseItem(2));
        tvItem4.setOnClickListener(v -> purchaseItem(3));
    }
    
    /**
     * Обрабатывает покупку товара (с примеркой)
     */
    private void purchaseItem(int index) {
        if (currentItems == null || index >= currentItems.size()) {
            return;
        }
        
        ShopItem item = currentItems.get(index);
        User user = userDao.getUserById(currentUserId);
        
        if (user == null) {
            android.widget.Toast.makeText(this, "Ошибка: пользователь не найден", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Проверяем, не куплен ли уже товар
        if (item.isPurchased == 1) {
            // Если уже куплен, просто применяем
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().putString("selectedBackground", item.name).apply();
            switchCategory(currentCategory);
            android.widget.Toast.makeText(this, "Фон применен!", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Проверяем, достаточно ли монет
        if (user.coins < item.price) {
            android.widget.Toast.makeText(this, "Недостаточно монет", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Показываем диалог с примеркой
        showPreviewDialog(item, user);
    }
    
    /**
     * Показывает диалог с примеркой фона перед покупкой
     */
    private void showPreviewDialog(ShopItem item, User user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Примерка фона: " + item.name);
        builder.setMessage("Цена: " + item.price + " монет\n\nВы хотите применить этот фон?");
        
        // Создаем View для примера
        android.view.View previewView = new android.view.View(this);
        Integer backgroundDrawable = BackgroundHelper.getBackgroundDrawable(item.name);
        if (backgroundDrawable != null) {
            previewView.setBackgroundResource(backgroundDrawable);
        } else {
            previewView.setBackgroundColor(getBackgroundColor(item.name));
        }
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 320);
        params.setMargins(24, 16, 24, 16);
        previewView.setLayoutParams(params);

        androidx.cardview.widget.CardView previewCard = new androidx.cardview.widget.CardView(this);
        previewCard.setRadius(24f);
        previewCard.setCardElevation(8f);
        previewCard.setUseCompatPadding(true);
        previewCard.addView(previewView);

        android.widget.TextView previewTitle = new android.widget.TextView(this);
        previewTitle.setText("Предпросмотр фона");
        previewTitle.setTextSize(18f);
        previewTitle.setTextColor(0xFF1A1A1A);
        previewTitle.setPadding(32, 8, 32, 8);

        android.widget.TextView previewHint = new android.widget.TextView(this);
        previewHint.setText("После покупки этот фон будет использоваться на игровых экранах.");
        previewHint.setTextSize(13f);
        previewHint.setTextColor(0xFF666666);
        previewHint.setPadding(32, 0, 32, 8);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(12, 16, 12, 8);
        layout.addView(previewTitle);
        layout.addView(previewCard);
        layout.addView(previewHint);

        builder.setView(layout);
        
        builder.setPositiveButton("Купить и применить", (dialog, which) -> {
            // Покупаем товар
            user.coins -= item.price;
            userDao.updateCoins(currentUserId, user.coins);
            
            item.isPurchased = 1;
            shopItemDao.updateShopItem(item);
            
            // Сохраняем выбранный фон в SharedPreferences
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().putString("selectedBackground", item.name).apply();
            
            // Обновляем отображение
            loadUserData();
            switchCategory(currentCategory);
            
            android.widget.Toast.makeText(this, "Товар куплен и применен!", android.widget.Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("Отмена", null);
        
        builder.show();
    }
    
    /**
     * Возвращает цвет фона по имени (для предпросмотра)
     */
    private int getBackgroundColor(String backgroundName) {
        switch (backgroundName) {
            case "Синий фон":
                return 0xFF2196F3; // Синий
            case "Звездный фон":
                return 0xFF0a0e27; // Темно-синий для звездного фона
            case "Красный фон":
                return 0xFFF44336; // Красный
            case "Фиолетовый фон":
                return 0xFF9C27B0; // Фиолетовый
            default:
                return 0xFFFFFFFF; // Белый по умолчанию
        }
    }

    /**
     * Переключает категорию товаров и обновляет отображение
     * @param category - название категории: "Питомец" или "Фон"
     */
    private void switchCategory(String category) {
        currentCategory = category;
        
        // Сбрасываем стиль всех категорий
        tvCategoryBackground.setTextColor(0xFF757575);
        
        // Выделяем выбранную категорию синим цветом
        switch (category) {
            case "Фон":
                tvCategoryBackground.setTextColor(getResources().getColor(R.color.primary_blue, null));
                // Загружаем товары из БД
                currentItems = shopItemDao.getShopItemsByType("background");
                // Обновляем названия товаров
                updateItemDisplay();
                break;
        }
    }
    
    /**
     * Обновляет отображение товаров
     */
    private void updateItemDisplay() {
        if (currentItems == null) {
            return;
        }
        
        // Получаем выбранный фон из настроек
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String selectedBackground = prefs.getString("selectedBackground", null);
        
        TextView[] itemViews = {tvItem1, tvItem2, tvItem3, tvItem4};
        
        for (int i = 0; i < itemViews.length; i++) {
            if (i < currentItems.size()) {
                ShopItem item = currentItems.get(i);
                String displayText = item.name;
                if (item.isPurchased == 1) {
                    if (item.name.equals(selectedBackground)) {
                        displayText += " (Выбрано)";
                        itemViews[i].setTextColor(getResources().getColor(R.color.primary_blue, null));
                    } else {
                        displayText += " (Куплено)";
                        itemViews[i].setTextColor(getResources().getColor(R.color.text_secondary, null));
                    }
                } else {
                    displayText += " (" + item.price + " монет)";
                    itemViews[i].setTextColor(getResources().getColor(R.color.black, null));
                }
                itemViews[i].setText(displayText);
            } else {
                itemViews[i].setText("");
            }
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
        // Обновляем отображение товаров
        switchCategory(currentCategory);
    }
}
