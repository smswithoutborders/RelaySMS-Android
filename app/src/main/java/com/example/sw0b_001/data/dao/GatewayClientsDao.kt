package com.example.sw0b_001.data.dao

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
    @get:Query("SELECT * FROM GatewayClients WHERE isDefault = 0 ORDER BY date DESC")
    val all: Flow<List<GatewayClients>>

    @get:Query("SELECT * FROM GatewayClients WHERE isDefault = 0 ORDER BY date DESC")
    val allList: List<GatewayClients>

    @Query("SELECT * FROM GatewayClients WHERE operatorCode = :operatorCode")
    fun findForOperatorCode(operatorCode: String?): MutableList<GatewayClients>?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(gatewayClients: List<GatewayClients>)

    @Transaction
    fun insertFresh(gatewayClients: List<GatewayClients>) {
        val currentDefault = getDefaultList()

        val remove = allList.filter { stored ->
            val notInNewList = gatewayClients.none { gwc -> gwc.msisdn == stored.msisdn }
            val isProtected = stored.possibleDefault || stored.manuallyAdded
            notInNewList && !isProtected
        }

        for (gwc in remove) {
            delete(gwc)
        }

        val preservedItems = allList - remove.toSet()
        val newList = (gatewayClients + preservedItems).distinctBy { it.msisdn }

        val defaultStillExists = currentDefault != null && newList.any { it.msisdn == currentDefault.msisdn }

        if (defaultStillExists) {
            newList.forEach { it.isDefault = it.msisdn == currentDefault?.msisdn }
        } else {
            newList.forEach { it.isDefault = false } // Clear all first

            val fallbackDefault = newList.firstOrNull { it.possibleDefault } ?: newList.firstOrNull()
            fallbackDefault?.isDefault = true
        }

        insert(newList)
    }
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertForce(gatewayClients: List<GatewayClients>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(gatewayClients: GatewayClients): Long

    @Delete
    fun delete(gatewayClients: GatewayClients)

    @Update
    fun update(gatewayClients: GatewayClients)

    @Query("UPDATE GatewayClients SET isDefault =0")
    fun resetAllDefaults()

    @Query("SELECT * FROM GatewayClients WHERE id = :id")
    fun fetch(id: Long): GatewayClients?

    @Query("SELECT * FROM GatewayClients WHERE isDefault = 1")
    fun fetchDefault(): GatewayClients?

    @Query("SELECT * FROM GatewayClients WHERE isDefault = 1")
    fun getDefault(): Flow<GatewayClients?>

    @Query("SELECT * FROM GatewayClients WHERE isDefault = 1")
    fun getDefaultList(): GatewayClients?

    @Transaction
    fun makeDefault(gatewayClient: GatewayClients) {
        resetAllDefaults()
        gatewayClient.isDefault = true
        update(gatewayClient)
    }

    @Query("SELECT * FROM GatewayClients WHERE MSISDN = :msisdn")
    fun getByAddress(msisdn: String): Flow<GatewayClients>
}
