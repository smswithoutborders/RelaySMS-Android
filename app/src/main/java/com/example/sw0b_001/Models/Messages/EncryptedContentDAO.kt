package com.example.sw0b_001.Models.Messages;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EncryptedContentDAO {

    @Insert
    long insert(EncryptedContent encryptedContent);

    @Query("SELECT * FROM EncryptedContent ORDER BY date DESC")
    LiveData<List<EncryptedContent>> getAll();

    @Query("DELETE FROM EncryptedContent")
    void deleteAll();

    @Query("DELETE FROM EncryptedContent WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM EncryptedContent WHERE id=:encryptedContentId")
    EncryptedContent get(long encryptedContentId);

    @Query("SELECT * FROM EncryptedContent WHERE encryptedContent LIKE '%' || :filterText || '%'")
    List<EncryptedContent> getForFilterText(String filterText);

}
