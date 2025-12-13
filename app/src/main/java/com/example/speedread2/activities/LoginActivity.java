package com.example.speedread2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.speedread2.R;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.dao.UserDao;
import com.example.speedread2.database.entities.User;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity для входа в приложение
 * Позволяет пользователю войти по email и паролю
 * или перейти к экрану регистрации
 */
public class LoginActivity extends AppCompatActivity {

    // Элементы интерфейса
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView btnRegister;
    
    // База данных
    private AppDatabase database;
    private UserDao userDao;

    /**
     * Вызывается при создании Activity
     * Инициализирует элементы интерфейса и настраивает обработчики событий
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализация базы данных
        database = AppDatabase.getInstance(this);
        userDao = database.userDao();

        // Инициализация элементов интерфейса
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Обработчик кнопки входа - выполняет авторизацию пользователя
        btnLogin.setOnClickListener(v -> {
            // Получаем введенные данные
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            
            // Валидация email - проверка на пустоту
            if (email.isEmpty()) {
                Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Валидация email - проверка формата
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Валидация пароля - проверка на пустоту
            if (password.isEmpty()) {
                Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Проверка данных пользователя в БД
            User user = userDao.loginUser(email, password);
            
            if (user != null) {
                // Сохраняем ID пользователя и состояние входа
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit()
                    .putInt("currentUserId", user.id)
                    .putBoolean("isLoggedIn", true)
                    .apply();
                
                // Переход на главный экран приложения
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("userId", user.id);
                startActivity(intent);
                finish();
            } else {
                // Показываем ошибку при неверных данных
                Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
            }
        });

        // Обработчик кнопки регистрации - переход на экран регистрации
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        });
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
