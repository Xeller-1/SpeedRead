package com.example.speedread2.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.speedread2.database.entities.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    void insertCategory(Category category);

    @Insert
    void insertCategories(List<Category> categories);

    @Query("SELECT * FROM categories")
    List<Category> getAllCategories();

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    Category getCategoryById(int id);

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    Category getCategoryByName(String name);
}

