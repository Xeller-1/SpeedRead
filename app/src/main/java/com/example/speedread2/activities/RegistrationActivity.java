package com.example.speedread2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.speedread2.R;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.dao.UserDao;
import com.example.speedread2.dao.UserStatsDao;
import com.example.speedread2.database.entities.User;
import com.example.speedread2.database.entities.UserStats;
import com.example.speedread2.utils.BackgroundHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity для регистрации нового пользователя
 * Позволяет ввести возраст, имя, email и пароль
 * Сохраняет данные и автоматически входит в приложение
 */
public class RegistrationActivity extends AppCompatActivity {

    // Элементы интерфейса
    private TextInputEditText etEmail, etPassword, etUsername;
    private Spinner spinnerAge;
    private Button btnRegister;
    private TextView btnBackToLogin;
    
    // База данных
    private AppDatabase database;
    private UserDao userDao;
    private UserStatsDao userStatsDao;

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        BackgroundHelper.applyBackground(this);

        // Инициализация базы данных
        database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        userStatsDao = database.userStatsDao();

        // Инициализация элементов интерфейса
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        spinnerAge = findViewById(R.id.spinnerAge);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        // Настройка спиннера с выбором возраста (от 13 до 100 лет)
        setupAgeSpinner();

        // Обработчик кнопки регистрации - выполняет регистрацию нового пользователя
        btnRegister.setOnClickListener(v -> {
            // Получаем введенные данные
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            String username = etUsername.getText().toString().trim();
            int age = (int) spinnerAge.getSelectedItem();
            
            // Валидация имени пользователя
            if (username.isEmpty()) {
                Toast.makeText(this, "Введите имя пользователя", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Валидация email
            if (email.isEmpty()) {
                Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Проверка формата email
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Валидация пароля
            if (password.isEmpty()) {
                Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Проверка минимальной длины пароля
            if (password.length() < 6) {
                Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Проверяем, не существует ли уже пользователь с таким email
            User existingUser = userDao.getUserByEmail(email);
            if (existingUser != null) {
                Toast.makeText(this, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Создаем нового пользователя
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            User newUser = new User(username, email, password, currentTime);
            newUser.coins = 100; // Начальные монеты
            newUser.level = 1;
            newUser.experience = 0;
            
            // Сохраняем пользователя в БД
            long userId = userDao.insertUser(newUser);
            
            // Создаем статистику для пользователя
            UserStats userStats = new UserStats((int) userId);
            userStats.lastUpdated = currentTime;
            userStatsDao.insertUserStats(userStats);
            
            // Сохраняем ID пользователя в SharedPreferences для быстрого доступа
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit()
                .putInt("currentUserId", (int) userId)
                .putBoolean("isLoggedIn", true)
                .apply();
            
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
            
            // Переход на главный экран приложения
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("userId", (int) userId);
            startActivity(intent);
            finish();
        });

        // Обработчик кнопки возврата к входу - возвращает на экран входа
        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Настраивает спиннер для выбора возраста
     * Создает список возрастов от 13 до 100 лет
     * Устанавливает возраст 18 по умолчанию
     */
    private void setupAgeSpinner() {
        // Создаем список возрастов от 13 до 100
        List<Integer> ages = new ArrayList<>();
        for (int i = 13; i <= 100; i++) {
            ages.add(i);
        }
        
        // Создаем адаптер для спиннера
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, ages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAge.setAdapter(adapter);
        
        // Устанавливаем возраст 18 по умолчанию
        spinnerAge.setSelection(ages.indexOf(18));
    }

    /**
     * Проверяет корректность формата email адреса
     * @param email - строка с email для проверки
     * @return true если email корректен, false в противном случае
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
