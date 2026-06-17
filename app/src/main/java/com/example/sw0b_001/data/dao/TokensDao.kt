package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.sw0b_001.data.models.Tokens
import kotlinx.coroutines.flow.Flow
import uniffi.relaysms_spec_payload.V1ContentCategories

@Dao
interface TokensDao {
    @Query("SELECT * FROM Tokens")
    fun fetchAll() : Flow<List<Tokens>>

    @Query("SELECT * FROM Tokens")
    fun fetchAllListDebug() : List<Tokens>

    @Query("SELECT * FROM Tokens WHERE catId = :catId")
    fun fetchCatId(catId: V1ContentCategories) : Flow<List<Tokens>>

    @Insert
    fun insert(tokens: Tokens)
}