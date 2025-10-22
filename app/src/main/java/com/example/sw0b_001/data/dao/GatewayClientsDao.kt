package com.example.sw0b_001.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.sw0b_001.data.models.GatewayClients
import kotlinx.coroutines.flow.Flow

@Dao
interface GatewayClientsDao {
    @get:Query("SELECT * FROM GatewayClients ORDER BY date DESC")
    val all: LiveData<List<GatewayClients>>

    @Query("SELECT * FROM GatewayClients WHERE operatorCode = :operatorCode")
    fun findForOperatorCode(operatorCode: String?): MutableList<GatewayClients>?

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun insertAll(gatewayClients: MutableList<GatewayClients>)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(gatewayClients: GatewayClients): Long

    @Delete
    fun delete(gatewayClients: GatewayClients)

    @Query("UPDATE GatewayClients SET isDefault = :setDefault WHERE id=:id")
    fun updateDefault(setDefault: Boolean, id: Long)

    @Update
    fun update(gatewayClients: GatewayClients)

    @Query("UPDATE GatewayClients SET isDefault =0")
    fun resetAllDefaults()

    @Query("SELECT * FROM GatewayClients WHERE id = :id")
    fun fetch(id: Long): GatewayClients?

    @Query("SELECT * FROM GatewayClients WHERE MSISDN = :msisdn")
    fun getByAddress(msisdn: String): Flow<GatewayClients>
}
