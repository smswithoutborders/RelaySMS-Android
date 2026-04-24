package com.example.sw0b_001

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.data.grpc.PublisherGrpcImpl
import com.example.sw0b_001.data.grpc.VaultsGrpcImpl
import com.example.sw0b_001.extensions.context.settingsGetStoreTokensOnDevice
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLDecoder


class OauthRedirectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_open_idoauth_redirect)

        val intentUrl = intent.dataString
        if(intentUrl.isNullOrEmpty()) {
            finish()
        }

        val parameters = Helpers.extractParameters(intentUrl!!)
        val decoded = String(Base64.decode(URLDecoder.decode(parameters["state"]!!, "UTF-8"),
            Base64.DEFAULT), Charsets.UTF_8)

        val values = decoded.split(",")
        val platform = values[0]
        val supportsUrlScheme = values[1] == "true"
        val code: String = URLDecoder.decode(parameters["code"]!!, "UTF-8")


        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            sendAuthCode(
                platform = platform,
                code = code,
                supportsUrlScheme = supportsUrlScheme
            )
        }
    }

    fun sendAuthCode(
        platform: String,
        code: String,
        supportsUrlScheme: Boolean,
    ) {
        val db = Datastore.getDatastore(applicationContext)?.keysDao()
            ?: throw Exception("Could not open database")

        val publisherPublicKey = db.fetchPublicKey(VaultsGrpcImpl.clientVaultHandshakeKeystoreAliasStaticKeys)
            ?: throw Exception("Missing private key in credentials for signing")
        val publisherGrpcImpl = PublisherGrpcImpl(applicationContext)
        try {
            val codeVerifier = PublisherGrpcImpl.fetchOauthRequestVerifier(applicationContext)
            val requestIdentifier = Base64.encodeToString(publisherPublicKey, Base64.NO_WRAP)

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val storeTokensOnDevice = sharedPreferences.getBoolean("store_tokens_on_device", false)

            if (storeTokensOnDevice) {
                publisherGrpcImpl.sendOAuthAuthorizationCode(
                    platform,
                    code,
                    codeVerifier,
                    supportsUrlScheme,
                    false,
                    requestIdentifier
                )
            } else {
                publisherGrpcImpl.sendOAuthAuthorizationCode(
                    platform,
                    code,
                    codeVerifier,
                    supportsUrlScheme,
                    requestIdentifier = requestIdentifier
                )
            }

            val vaultsGrpcImpl = VaultsGrpcImpl(applicationContext)
            vaultsGrpcImpl.refreshStoredTokens(
                applicationContext,
                settingsGetStoreTokensOnDevice)
            vaultsGrpcImpl.shutdown()
        } catch(e: StatusRuntimeException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(applicationContext, e.status.description, Toast.LENGTH_LONG).show()
            }
        }
        catch(e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        } finally {
            publisherGrpcImpl.shutdown()
        }

        runOnUiThread {
            val isOnboarding = intent.getBooleanExtra("is_onboarding",
                false)
            val intent = Intent( applicationContext,
                MainActivity::class.java)
                .apply {
                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    setPackage(packageName)
                    putExtra("is_onboarding", isOnboarding)
                }
            startActivity(intent)
            finish()
        }

    }

}