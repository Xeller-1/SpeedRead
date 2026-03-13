package com.example.speedread2.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.speedread2.database.entities.TongueTwister;

import java.util.List;

@Dao
public interface TongueTwisterDao {
    @Insert
    void insertTongueTwister(TongueTwister tongueTwister);

    @Insert
    void insertTongueTwisters(List<TongueTwister> tongueTwisters);

    @Query("SELECT * FROM tongue_twisters")
    List<TongueTwister> getAllTongueTwisters();

    @Query("SELECT * FROM tongue_twisters WHERE id = :id LIMIT 1")
    TongueTwister getTongueTwisterById(int id);

    @Query("SELECT * FROM tongue_twisters WHERE difficulty = :difficulty")
    List<TongueTwister> getTongueTwistersByDifficulty(int difficulty);
}

