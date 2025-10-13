package com.example.sw0b_001.data;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.DeleteColumn;
import androidx.room.DeleteTable;
import androidx.room.InvalidationTracker;
import androidx.room.RenameTable;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.AutoMigrationSpec;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.example.sw0b_001.data.dao.EncryptedContentDAO;
import com.example.sw0b_001.data.dao.GatewayClientsDao;
import com.example.sw0b_001.data.models.EncryptedContent;
import com.example.sw0b_001.data.models.GatewayClients;
import com.example.sw0b_001.data.models.GatewayServer;
import com.example.sw0b_001.data.dao.GatewayServersDAO;
import com.example.sw0b_001.data.dao.RatchetStatesDAO;
import com.example.sw0b_001.data.models.AvailablePlatforms;
import com.example.sw0b_001.data.dao.AvailablePlatformsDao;
import com.example.sw0b_001.data.models.Platforms;
import com.example.sw0b_001.data.dao.PlatformDao;
import com.example.sw0b_001.data.dao.StoredPlatformsDao;
import com.example.sw0b_001.data.models.StoredPlatformsEntity;
import com.example.sw0b_001.data.models.RatchetStates;

import org.jetbrains.annotations.NotNull;

@Database(entities = {
        RatchetStates.class,
        GatewayServer.class,
        Platforms.class,
        AvailablePlatforms.class,
        GatewayClients.class,
        StoredPlatformsEntity.class,
        EncryptedContent.class,},
        version = 23,
        autoMigrations = {
        @AutoMigration( from = 8, to = 9, spec = Datastore.DatastoreMigrations.class),
        @AutoMigration( from = 9, to = 10, spec= Datastore.DatastoreMigrations.class),
        @AutoMigration( from = 10, to = 11),
        @AutoMigration( from = 11, to = 12),
        @AutoMigration( from = 12, to = 13),
        @AutoMigration( from = 13, to = 14),
        @AutoMigration( from = 14, to = 15),
        @AutoMigration( from = 15, to = 16),
        @AutoMigration( from = 16, to = 17),
        @AutoMigration( from = 17, to = 18, spec = Datastore.Migrate17To18.class),
        @AutoMigration( from = 18, to = 19),
        @AutoMigration( from = 19, to = 20),
        @AutoMigration( from = 20, to = 21),
        @AutoMigration( from = 21, to = 22, spec = Datastore.Migrate21To22.class),
        @AutoMigration( from = 22, to = 23, spec = Datastore.Migrate22To23.class),
})

public abstract class Datastore extends RoomDatabase {
    @RenameTable(fromTableName = "Platform", toTableName = "Platforms")
    @DeleteTable(tableName = "Notifications")
    static class DatastoreMigrations implements AutoMigrationSpec { }

    public static String databaseName = "SMSWithoutBorders-Android-App-DB";
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
    public abstract GatewayServersDAO gatewayServersDAO();
    public abstract EncryptedContentDAO encryptedContentDAO();
    public abstract StoredPlatformsDao storedPlatformsDao();
    public abstract RatchetStatesDAO ratchetStatesDAO();

    @NonNull
    @NotNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @NonNull
    @NotNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @DeleteColumn.Entries({
            @DeleteColumn(tableName = "EncryptedContent", columnName = "platform_id"),
    })
    static class Migrate17To18 implements AutoMigrationSpec { }

    static class Migrate21To22 implements AutoMigrationSpec {
        @Override
        public void onPostMigrate(@NonNull SupportSQLiteDatabase db) {
            AutoMigrationSpec.super.onPostMigrate(db);
            db.compileStatement("DELETE FROM EncryptedContent").executeUpdateDelete();
        }
    }

    @DeleteTable.Entries({
            @DeleteTable(tableName = "GatewayClient")
    })
    static class Migrate22To23 implements AutoMigrationSpec {
        @Override
        public void onPostMigrate(@NonNull SupportSQLiteDatabase db) {
        }
    }
}
