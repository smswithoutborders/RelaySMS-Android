package com.example.sw0b_001

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.data.grpc.PublisherGrpcImpl
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
        val decoded = String(
            Base64.decode(
                URLDecoder.decode(parameters["state"]!!, "UTF-8"),
                Base64.DEFAULT
            ), Charsets.UTF_8)

        val values = decoded.split(",")
        val platform = values[0]
        val code: String = URLDecoder.decode(parameters["code"]!!, "UTF-8")


        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            sendAuthCode(
                platformName = platform,
                code = code,
            )
        }
    }

    suspend fun sendAuthCode(
        platformName: String,
        code: String,
    ) {
        PublisherGrpcImpl(applicationContext).use { publisherGrpcImpl ->
            try {
                val oAuth = PublisherGrpcImpl
                    .fetchOauthRequestVerifier(applicationContext, platformName)
                oAuth.use { oa ->
                    try {
                        val response = publisherGrpcImpl.sendOAuthAuthorizationCode(
                            platform = platformName,
                            code = code,
                            codeVerifier = String(oa.codeVerifier),
                            requestIdentifier = Base64
                                .encodeToString(oa.requestId, Base64.NO_WRAP)
                        )

                        val tokenHash = TODO("Get token Hash from here")
                        val serverEphemeralKeys = TODO("Get token Hash from here")
                        TODO("Store the server's ephemeral keys")

//                        VaultsGrpcImpl(applicationContext).use { vaultsGrpcImpl ->
//                            try {
//                                val (tokenId, keys) = vaultsGrpcImpl.uploadKeys(tokenHash)
//                                Keys.save(applicationContext, tokenHash, keys, tokenId)
//                            } catch(e: Exception) {
//                                throw e;
//                            } finally {
//                                tokenHash.fill(0)
//                            }
//                        }
                    } finally {
                        oAuth.clear(applicationContext)
                    }
                }
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
            }
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