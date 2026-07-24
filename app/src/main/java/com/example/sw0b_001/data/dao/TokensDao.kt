package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.ui.viewModels.TokensMetrics
import kotlinx.coroutines.flow.Flow

@Dao
interface TokensDao {
    @Query("SELECT * FROM Tokens")
    fun fetchAll() : Flow<List<Tokens>>

    @Query("SELECT * FROM Tokens")
    suspend fun fetchAllList() : List<Tokens>

    @Query("SELECT * FROM Tokens where tokenHash = :tokenHash")
    fun fetch(tokenHash: ByteArray) : Tokens?

    @Query("SELECT * FROM Tokens where id = :id")
    fun fetch(id: Long) : Tokens

    @Query("SELECT * FROM Tokens WHERE platformName = :name")
    fun fetch(name: String) : Flow<List<Tokens>>

    @Insert
    fun insert(tokens: Tokens): Long

    @Insert
    fun insert(tokens: List<Tokens>)

    @Delete
    fun delete(tokens: Tokens)

    @Query("DELETE FROM Tokens")
    suspend fun deleteAll()


    @Query("SELECT account, date, keys.* FROM Tokens, " +
            "(SELECT " +
            "COUNT(CASE WHEN alias = :aliasServer OR alias = :alias THEN 1 END) as quantityEncryptionKeysServer, " +
            "COUNT(CASE WHEN alias = :aliasClient THEN 1 END) as quantityEncryptionKeysClient, " +
            "COUNT(CASE WHEN alias = :aliasServer THEN 1 END) as quantityText, " +
            "COUNT(CASE WHEN alias = :alias THEN 1 END) as quantityAttachments, " +
            "date as lastSync " +
            "FROM Keys WHERE tokenId = :tokenId) as keys " +
            "WHERE id = :tokenId")
    fun getTokensMetrics(
        tokenId: Long,
        alias: String,
        aliasClient: String,
        aliasServer: String,
    ): Flow<TokensMetrics>
}