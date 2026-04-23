package com.example.sw0b_001.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.afkanerd.smswithoutborders_libsmsmms.data.Cryptography.getDatabasePassword
import com.example.sw0b_001.data.dao.AvailablePlatformsDao
import com.example.sw0b_001.data.dao.GatewayClientsDao
import com.example.sw0b_001.data.dao.KeysDao
import com.example.sw0b_001.data.dao.MessagesDao
import com.example.sw0b_001.data.dao.OAuthDao
import com.example.sw0b_001.data.dao.PlatformDao
import com.example.sw0b_001.data.dao.RatchetStatesDAO
import com.example.sw0b_001.data.dao.StoredPlatformsDao
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.data.models.GatewayClients
import com.example.sw0b_001.data.models.Keys
import com.example.sw0b_001.data.models.Messages
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.RatchetStates
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory


@Database(
    entities = [
        RatchetStates::class,
        Platforms::class,
        AvailablePlatforms::class,
        GatewayClients::class,
        StoredPlatformsEntity::class,
        OAuth::class,
        Keys::class,
        Messages::class],
    version = 1,
    exportSchema = true,
    autoMigrations = []
)
abstract class Datastore : RoomDatabase() {
    init {
        System.loadLibrary("sqlcipher")
    }

    abstract fun platformDao(): PlatformDao?
    abstract fun availablePlatformsDao(): AvailablePlatformsDao?
    abstract fun gatewayClientsDao(): GatewayClientsDao?
    abstract fun encryptedContentDAO(): MessagesDao?
    abstract fun storedPlatformsDao(): StoredPlatformsDao?
    abstract fun ratchetStatesDAO(): RatchetStatesDAO?
    abstract fun keysDao(): KeysDao?
    abstract fun oAuthDao(): OAuthDao?

    companion object {
        private var datastore: Datastore? = null

        fun getDatastore(context: Context): Datastore? {
            if (datastore == null) {
                val dbKeystoreAlias = "RelaySMS_KeystoreAlias"
                val databaseName = "smswithoutborders_relaysms.db"

                getDatabasePassword(context, dbKeystoreAlias).use { password ->
                    val databaseFile = context.getDatabasePath(databaseName)

                    password.useRaw { rawBytes ->
                        datastore = databaseBuilder(
                            context = context,
                            klass = Datastore::class.java,
                            databaseFile.absolutePath,
                        )
                            .openHelperFactory(SupportOpenHelperFactory(rawBytes))
                            .fallbackToDestructiveMigration(false)
                            .build()
                    }
                }

            }

            return datastore
        }
    }
}
