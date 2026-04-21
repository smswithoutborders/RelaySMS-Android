package com.example.sw0b_001.data;


import android.content.Context;

import androidx.room.Database;
import androidx.room.DeleteTable;
import androidx.room.RenameTable;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.AutoMigrationSpec;

import com.example.sw0b_001.data.dao.CredentialsDao;
import com.example.sw0b_001.data.dao.MessagesDao;
import com.example.sw0b_001.data.dao.GatewayClientsDao;
import com.example.sw0b_001.data.dao.KeysDao;
import com.example.sw0b_001.data.dao.SecurityKeystoreDao;
import com.example.sw0b_001.data.models.Credentials;
import com.example.sw0b_001.data.models.Messages;
import com.example.sw0b_001.data.models.GatewayClients;
import com.example.sw0b_001.data.dao.RatchetStatesDAO;
import com.example.sw0b_001.data.models.AvailablePlatforms;
import com.example.sw0b_001.data.dao.AvailablePlatformsDao;
import com.example.sw0b_001.data.models.Keys;
import com.example.sw0b_001.data.models.Platforms;
import com.example.sw0b_001.data.dao.PlatformDao;
import com.example.sw0b_001.data.dao.StoredPlatformsDao;
import com.example.sw0b_001.data.models.SecurityKeys;
import com.example.sw0b_001.data.models.StoredPlatformsEntity;
import com.example.sw0b_001.data.models.RatchetStates;

@Database(entities = {
        RatchetStates.class,
        Platforms.class,
        AvailablePlatforms.class,
        GatewayClients.class,
        StoredPlatformsEntity.class,
        Keys.class,
        Credentials.class,
        Messages.class,},
        version = 1,
        autoMigrations = { }
)

public abstract class Datastore extends RoomDatabase {
    @RenameTable(fromTableName = "Platform", toTableName = "Platforms")
    @DeleteTable(tableName = "Notifications")
    static class DatastoreMigrations implements AutoMigrationSpec { }

    public static String databaseName = "smswithoutborders_relaysms.db";
    private static Datastore datastore;

    public static Datastore getDatastore(Context context) {
        if(datastore == null || !datastore.isOpen()) {
            datastore = Room.databaseBuilder(context, Datastore.class, databaseName)
                    .enableMultiInstanceInvalidation()
                    .build();
        }

        return datastore;
    }


    public abstract PlatformDao platformDao();
    public abstract AvailablePlatformsDao availablePlatformsDao();
    public abstract GatewayClientsDao gatewayClientsDao();
    public abstract MessagesDao encryptedContentDAO();
    public abstract StoredPlatformsDao storedPlatformsDao();
    public abstract RatchetStatesDAO ratchetStatesDAO();
    public abstract KeysDao keysDao();
    public abstract CredentialsDao credentialsDao();
}
