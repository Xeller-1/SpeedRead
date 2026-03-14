package com.example.speedread2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.speedread2.R;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.dao.UserDao;
import com.example.speedread2.database.entities.User;
import com.example.speedread2.utils.BackgroundHelper;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvPassword;
    private TextView tvBestSpeed, tvBestClarity, tvBestUnderstanding;
    private TextInputEditText etEmailEdit, etPasswordEdit, etUsernameEdit;
    private ImageButton btnShop, btnBack;
    private ImageButton btnEditEmail, btnSaveEmail, btnCancelEmail;
    private ImageButton btnEditPassword, btnSavePassword, btnCancelPassword;
    private ImageButton btnEditUsername, btnSaveUsername, btnCancelUsername;
    private Button btnLogout;
    private String originalEmail, originalPassword, originalUsername;
    
    // База данных
    private AppDatabase database;
    private UserDao userDao;
    private int currentUserId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        BackgroundHelper.applyBackground(this);
        
        // Инициализация базы данных
        database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        
        // Получаем ID текущего пользователя
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("currentUserId", -1);

        // Инициализация текстовых полей для отображения данных
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPassword = findViewById(R.id.tvPassword);

        tvBestClarity = findViewById(R.id.tvBestClarity);
        tvBestSpeed = findViewById(R.id.tvBestSpeed);
        tvBestUnderstanding = findViewById(R.id.tvBestUnderstanding);

        // Инициализация полей ввода для редактирования
        etEmailEdit = findViewById(R.id.etEmailEdit);
        etPasswordEdit = findViewById(R.id.etPasswordEdit);
        etUsernameEdit = findViewById(R.id.etUsernameEdit);
        
        // Инициализация кнопок навигации
        btnShop = findViewById(R.id.btnShop);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
        
        // Инициализация кнопок редактирования email
        btnEditEmail = findViewById(R.id.btnEditEmail);
        btnSaveEmail = findViewById(R.id.btnSaveEmail);
        btnCancelEmail = findViewById(R.id.btnCancelEmail);
        
        // Инициализация кнопок редактирования пароля
        btnEditPassword = findViewById(R.id.btnEditPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        btnCancelPassword = findViewById(R.id.btnCancelPassword);
        
        // Инициализация кнопок редактирования никнейма
        btnEditUsername = findViewById(R.id.btnEditUsername);
        btnSaveUsername = findViewById(R.id.btnSaveUsername);
        btnCancelUsername = findViewById(R.id.btnCancelUsername);

        // Загружаем данные пользователя из SharedPreferences
        loadUserData();

        // Обработчик кнопки назад - возвращает на предыдущий экран
        btnBack.setOnClickListener(v -> finish());

        // Обработчик кнопки магазина - открывает экран магазина
        btnShop.setOnClickListener(v -> {
            Intent intent = new Intent(this, ShopActivity.class);
            startActivity(intent);
        });

        // Обработчик кнопки выхода - показывает диалог подтверждения
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        
        // Загружаем данные пользователя
        loadUserData();

        // Обработчики редактирования email
        btnEditEmail.setOnClickListener(v -> startEditingEmail());
        btnSaveEmail.setOnClickListener(v -> saveEmail());
        btnCancelEmail.setOnClickListener(v -> cancelEditingEmail());

        // Обработчики редактирования пароля
        btnEditPassword.setOnClickListener(v -> startEditingPassword());
        btnSavePassword.setOnClickListener(v -> savePassword());
        btnCancelPassword.setOnClickListener(v -> cancelEditingPassword());
        
        // Обработчики редактирования никнейма
        btnEditUsername.setOnClickListener(v -> startEditingUsername());
        btnSaveUsername.setOnClickListener(v -> saveUsername());
        btnCancelUsername.setOnClickListener(v -> cancelEditingUsername());
    }

    /**
     * Показывает диалог подтверждения выхода из профиля
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Выход из профиля")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Выйти", (dialog, which) -> {
                logout();
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    /**
     * Выполняет выход из профиля:
     * - Очищает флаг входа в SharedPreferences
     * - Перенаправляет на экран входа
     * - Очищает стек активности
     */
    private void logout() {
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        // Очищаем флаг входа и ID пользователя
        prefs.edit()
            .putBoolean("isLoggedIn", false)
            .remove("currentUserId")
            .apply();
        
        // Переход на экран входа с очисткой стека активности
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Загружает данные пользователя из БД и отображает их в интерфейсе
     */
    private void loadUserData() {
        if (currentUserId == -1) {
            return;
        }
        
        // Загружаем пользователя из БД
        currentUser = userDao.getUserById(currentUserId);
        
        if (currentUser != null) {
            // Отображаем данные в текстовых полях
            tvUsername.setText(currentUser.username);
            tvEmail.setText(currentUser.email);

            var data = database.userStatsDao().getUserStats(currentUserId);

            tvBestSpeed.setText("Лучшая скорость: " + String.valueOf(data.readingSpeed));
            tvBestClarity.setText("Лучшая четкость: " + String.valueOf(data.clarity));
            tvBestUnderstanding.setText("Лучшее понимание: " + String.valueOf(data.expression));

            // Показываем пароль как точки для безопасности
            if (currentUser.password != null && !currentUser.password.isEmpty()) {
                StringBuilder hiddenPassword = new StringBuilder();
                for (int i = 0; i < currentUser.password.length(); i++) {
                    hiddenPassword.append("•");
                }
                tvPassword.setText(hiddenPassword.toString());
            } else {
                tvPassword.setText("");
            }
        }
    }

    /**
     * Начинает редактирование никнейма пользователя
     */
    private void startEditingUsername() {
        // Сохраняем исходное значение никнейма
        originalUsername = tvUsername.getText().toString();
        // Скрываем текстовое поле и показываем поле ввода
        tvUsername.setVisibility(View.GONE);
        etUsernameEdit.setVisibility(View.VISIBLE);
        etUsernameEdit.setText(originalUsername);
        // Скрываем кнопку редактирования, показываем кнопки сохранения и отмены
        btnEditUsername.setVisibility(View.GONE);
        btnSaveUsername.setVisibility(View.VISIBLE);
        btnCancelUsername.setVisibility(View.VISIBLE);
    }

    /**
     * Сохраняет изменения никнейма пользователя
     */
    private void saveUsername() {
        String newUsername = etUsernameEdit.getText().toString().trim();
        
        // Проверяем, что никнейм не пустой
        if (TextUtils.isEmpty(newUsername)) {
            Toast.makeText(this, "Имя пользователя не может быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Обновляем никнейм в БД
        if (currentUser != null) {
            currentUser.username = newUsername;
            userDao.updateUser(currentUser);
            
            // Обновляем отображение и завершаем редактирование
            tvUsername.setText(newUsername);
            cancelEditingUsername();
            Toast.makeText(this, "Имя пользователя обновлено", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Отменяет редактирование никнейма и возвращает исходное значение
     */
    private void cancelEditingUsername() {
        // Возвращаем текстовое поле и скрываем поле ввода
        tvUsername.setVisibility(View.VISIBLE);
        etUsernameEdit.setVisibility(View.GONE);
        // Показываем кнопку редактирования, скрываем кнопки сохранения и отмены
        btnEditUsername.setVisibility(View.VISIBLE);
        btnSaveUsername.setVisibility(View.GONE);
        btnCancelUsername.setVisibility(View.GONE);
    }

    /**
     * Начинает редактирование email пользователя
     */
    private void startEditingEmail() {
        // Сохраняем исходное значение email
        originalEmail = tvEmail.getText().toString();
        // Скрываем текстовое поле и показываем поле ввода
        tvEmail.setVisibility(View.GONE);
        etEmailEdit.setVisibility(View.VISIBLE);
        etEmailEdit.setText(originalEmail);
        // Скрываем кнопку редактирования, показываем кнопки сохранения и отмены
        btnEditEmail.setVisibility(View.GONE);
        btnSaveEmail.setVisibility(View.VISIBLE);
        btnCancelEmail.setVisibility(View.VISIBLE);
    }

    /**
     * Сохраняет изменения email пользователя с валидацией
     */
    private void saveEmail() {
        String newEmail = etEmailEdit.getText().toString().trim();
        
        // Проверяем, что email не пустой
        if (TextUtils.isEmpty(newEmail)) {
            Toast.makeText(this, "Email не может быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Проверяем корректность формата email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Проверяем, не занят ли email другим пользователем
        User existingUser = userDao.getUserByEmail(newEmail);
        if (existingUser != null && existingUser.id != currentUserId) {
            Toast.makeText(this, "Email уже используется другим пользователем", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Обновляем email в БД
        if (currentUser != null) {
            currentUser.email = newEmail;
            userDao.updateUser(currentUser);
            
            // Обновляем отображение и завершаем редактирование
            tvEmail.setText(newEmail);
            cancelEditingEmail();
            Toast.makeText(this, "Email обновлен", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Отменяет редактирование email и возвращает исходное значение
     */
    private void cancelEditingEmail() {
        // Возвращаем текстовое поле и скрываем поле ввода
        tvEmail.setVisibility(View.VISIBLE);
        etEmailEdit.setVisibility(View.GONE);
        // Показываем кнопку редактирования, скрываем кнопки сохранения и отмены
        btnEditEmail.setVisibility(View.VISIBLE);
        btnSaveEmail.setVisibility(View.GONE);
        btnCancelEmail.setVisibility(View.GONE);
    }

    /**
     * Начинает редактирование пароля пользователя
     */
    private void startEditingPassword() {
        // Сохраняем исходный пароль
        if (currentUser != null) {
            originalPassword = currentUser.password;
        }
        // Скрываем текстовое поле и показываем поле ввода (пароль начинается пустым)
        tvPassword.setVisibility(View.GONE);
        etPasswordEdit.setVisibility(View.VISIBLE);
        etPasswordEdit.setText("");
        // Скрываем кнопку редактирования, показываем кнопки сохранения и отмены
        btnEditPassword.setVisibility(View.GONE);
        btnSavePassword.setVisibility(View.VISIBLE);
        btnCancelPassword.setVisibility(View.VISIBLE);
    }

    /**
     * Сохраняет изменения пароля пользователя с валидацией
     */
    private void savePassword() {
        String newPassword = etPasswordEdit.getText().toString();
        
        // Проверяем, что пароль не пустой
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Пароль не может быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Проверяем минимальную длину пароля
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Обновляем пароль в БД
        if (currentUser != null) {
            currentUser.password = newPassword;
            userDao.updateUser(currentUser);
            
            // Обновляем отображение пароля (перезагружаем данные)
            loadUserData();
            cancelEditingPassword();
            Toast.makeText(this, "Пароль обновлен", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Отменяет редактирование пароля и возвращает исходное значение
     */
    private void cancelEditingPassword() {
        // Возвращаем текстовое поле и скрываем поле ввода
        tvPassword.setVisibility(View.VISIBLE);
        etPasswordEdit.setVisibility(View.GONE);
        etPasswordEdit.setText("");
        // Показываем кнопку редактирования, скрываем кнопки сохранения и отмены
        btnEditPassword.setVisibility(View.VISIBLE);
        btnSavePassword.setVisibility(View.GONE);
        btnCancelPassword.setVisibility(View.GONE);
    }

    /**
     * Вызывается при возврате на этот экран
     * Обновляет данные пользователя для актуального отображения
     */
    @Override
    protected void onResume() {
        super.onResume();
        BackgroundHelper.applyBackground(this);
        // Обновляем данные при возврате на экран (на случай изменений в других Activity)
        loadUserData();
    }
}
