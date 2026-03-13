package com.example.speedread2.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.speedread2.database.entities.ShopItem;

import java.util.List;

@Dao
public interface ShopItemDao {
    @Insert
    void insertShopItem(ShopItem shopItem);

    @Insert
    void insertShopItems(List<ShopItem> shopItems);

    @Update
    void updateShopItem(ShopItem shopItem);

    @Query("SELECT * FROM shop_items")
    List<ShopItem> getAllShopItems();

    @Query("SELECT * FROM shop_items WHERE id = :id LIMIT 1")
    ShopItem getShopItemById(int id);

    @Query("SELECT * FROM shop_items WHERE type = :type")
    List<ShopItem> getShopItemsByType(String type);

    @Query("SELECT * FROM shop_items WHERE is_purchased = 1")
    List<ShopItem> getPurchasedItems();

    @Query("UPDATE shop_items SET is_purchased = 1 WHERE id = :itemId")
    void markAsPurchased(int itemId);
}

