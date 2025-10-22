package com.example.sw0b_001.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.example.sw0b_001.data.models.GatewayServer;

@Dao
public interface GatewayServersDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GatewayServer gatewayServer);
}
