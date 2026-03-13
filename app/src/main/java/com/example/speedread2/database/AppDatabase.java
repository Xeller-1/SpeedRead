package com.example.speedread2.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.speedread2.dao.CategoryDao;
import com.example.speedread2.dao.QuestionDao;
import com.example.speedread2.dao.ShopItemDao;
import com.example.speedread2.dao.TextDao;
import com.example.speedread2.dao.TongueTwisterDao;
import com.example.speedread2.dao.UserDao;
import com.example.speedread2.dao.UserStatsDao;
import com.example.speedread2.database.entities.Category;
import com.example.speedread2.database.entities.Question;
import com.example.speedread2.database.entities.ShopItem;
import com.example.speedread2.database.entities.Text;
import com.example.speedread2.database.entities.TongueTwister;
import com.example.speedread2.database.entities.User;
import com.example.speedread2.database.entities.UserStats;

@Database(
    entities = {
        User.class,
        Category.class,
        Text.class,
        TongueTwister.class,
        ShopItem.class,
        UserStats.class,
        Question.class
    },
    version = 4,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract TextDao textDao();
    public abstract TongueTwisterDao tongueTwisterDao();
    public abstract ShopItemDao shopItemDao();
    public abstract UserStatsDao userStatsDao();
    public abstract QuestionDao questionDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "speedread_database"
            )
            .allowMainThreadQueries() // Для упрощения, в продакшене лучше использовать корутины/потоки
            .fallbackToDestructiveMigration() // При изменении версии БД удаляет старую
            .build();
        }
        return instance;
    }
}

