package com.example.sw0b_001.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.sw0b_001.data.models.GatewayClient

@Dao
interface GatewayClientsDao {
    @get:Query("SELECT * FROM GatewayClient ORDER BY date DESC")
    val all: LiveData<List<GatewayClient>>

    @Query("SELECT * FROM GatewayClient WHERE operator_id=:operator_id")
    fun findForOperaetorId(operator_id: String?): MutableList<GatewayClient>?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertAll(gatewayClients: MutableList<GatewayClient>)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(GatewayClient: GatewayClient): Long

    @Delete
    fun delete(GatewayClient: GatewayClient)

    @Query(
        "DELETE FROM GatewayClient WHERE type IS NOT 'custom' AND type is NOT 'default' AND NOT " +
                "EXISTS (SELECT * FROM GatewayClient WHERE mSISDN IN (:gatewayClients))"
    )
    fun clear(gatewayClients: MutableList<String>)

    @Query("UPDATE GatewayClient SET `default` = :setDefault WHERE id=:id")
    fun updateDefault(setDefault: Boolean, id: Long)

    @Update
    fun update(gatewayClient: GatewayClient)

    @Query("UPDATE GatewayClient SET `default`=0")
    fun resetAllDefaults()

    @Query("SELECT * FROM GatewayClient WHERE id = :id")
    fun fetch(id: Long): GatewayClient?

    @Transaction
    fun refresh(gatewayClients: MutableList<GatewayClient>) {
        val msisdns: MutableList<String> = ArrayList<String>()
        for (gatewayClient in gatewayClients) {
            if(gatewayClient.mSISDN != null)
                msisdns.add(gatewayClient.mSISDN!!)
        }
        clear(msisdns)
        insertAll(gatewayClients)
    }

    @Query("SELECT * FROM GatewayClient WHERE MSISDN = :msisdn")
    fun getByMsisdn(msisdn: String): GatewayClient?
}
