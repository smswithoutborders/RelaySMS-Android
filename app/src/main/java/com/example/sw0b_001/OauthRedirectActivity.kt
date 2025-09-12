package com.example.sw0b_001

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.sw0b_001.data.Publishers
import com.example.sw0b_001.data.Vaults
import com.example.sw0b_001.data.Helpers
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.net.URLDecoder
import android.net.Uri


class OauthRedirectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_open_idoauth_redirect)
        Helpers.logIntentDetails(intent)

        /**
         * Send this to Vault to complete the OAuth process
         */

        val intentUrl = intent.dataString
        if(intentUrl.isNullOrEmpty()) {
            Log.e(javaClass.name, "Intent has no URL")
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
            val publishers = Publishers(applicationContext)
            try {
                val llt = Vaults.fetchLongLivedToken(applicationContext)
                val codeVerifier = Publishers.fetchOauthRequestVerifier(applicationContext)
                val publisherPublicKey = Publishers.fetchPublisherPublicKey(context = applicationContext)
                val requestIdentifier = Base64.encodeToString(publisherPublicKey, Base64.NO_WRAP)

                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val storeTokensOnDevice = sharedPreferences.getBoolean("store_tokens_on_device", false)
                Log.d("Oauth redirect", "Store on device is $storeTokensOnDevice")

                if (storeTokensOnDevice) {
                    Log.d("Oauth redirect", "Store on device is true")
                    publishers.sendOAuthAuthorizationCode(
                        llt,
                        platform,
                        code,
                        codeVerifier,
                        supportsUrlScheme,
                        false,
                        requestIdentifier
                    )
                } else {
                    publishers.sendOAuthAuthorizationCode(
                        llt,
                        platform,
                        code,
                        codeVerifier,
                        supportsUrlScheme,
                        requestIdentifier = requestIdentifier
                    )
                }

                val vaults = Vaults(applicationContext)
                vaults.refreshStoredTokens(applicationContext)
                vaults.shutdown()
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
                publishers.shutdown()
            }
            runOnUiThread {
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
            }
        }
    }

}