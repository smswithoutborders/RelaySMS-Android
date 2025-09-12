package com.example.sw0b_001.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.sw0b_001.data.models.Platforms;

import java.util.List;

@Dao
public interface PlatformDao {
    @Query("SELECT * FROM Platforms")
    LiveData<List<Platforms>> getAll();

    @Query("SELECT * FROM Platforms WHERE isSaved = 1")
    LiveData<List<Platforms>> getSaved();

    @Query("SELECT * FROM Platforms WHERE isSaved = 0")
    LiveData<List<Platforms>> getUnSaved();

    @Query("SELECT * FROM Platforms")
    List<Platforms> getAllList();

    @Query("SELECT * FROM Platforms WHERE id=:platform_id")
    Platforms get(long platform_id);

    @Query("SELECT * FROM Platforms WHERE name=:platformName")
    Platforms get(String platformName);

    @Query("SELECT * FROM Platforms WHERE type = :type LIMIT 1")
    Platforms getType(String type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Platforms> platforms);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Platforms platforms);

    @Delete
    void delete(Platforms platforms);

    @Query("DELETE FROM Platforms")
    void deleteAll();

    @Query("SELECT COUNT(Platforms.id) as count FROM platforms")
    int count();

    @Query("SELECT COUNT(Platforms.id) as count FROM platforms WHERE isSaved = 1")
    int countSaved();

}
