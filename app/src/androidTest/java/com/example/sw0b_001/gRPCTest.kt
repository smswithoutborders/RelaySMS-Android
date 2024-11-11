package com.example.sw0b_001

import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Publisher
import com.example.sw0b_001.Models.Vault
import com.example.sw0b_001.Modules.Network
import com.example.sw0b_001.Security.Cryptography
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import publisher.v1.PublisherGrpc.PublisherBlockingStub
import vault.v1.EntityGrpc.EntityBlockingStub

/**
 * Flow from https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md
 * 1. Create Account
 * https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md#create-an-entity
 *
 * 2. Complete Account creation
 * https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md#complete-creation
 *
 * 3. Authenticate an entity
 * https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md#authenticate-an-entity
 *
 * 4. Store entities
 * https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md#store-an-entitys-token
 *
 * 5. List stored entities
 * https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md#list-an-entitys-stored-tokens
 */

/**
 * Docs: https://grpc.github.io/grpc/core/md_doc_statuscodes.html
 */
class gRPCTest {
    private val globalPhoneNumber = "+23711234575"
    private val globalCountryCode = "CM"
    private val globalPassword = "dMd2Kmo9#"

    private lateinit var deviceIdPubKey: ByteArray
    private lateinit var publishPubKey: ByteArray


    private var context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var vault: Vault
    private val device_id_keystoreAlias = "device_id_keystoreAlias"
    private val publisher_keystoreAlias = "publisher_keystoreAlias"

    @Before
    fun init() {
        vault = Vault(context)
        deviceIdPubKey = Cryptography.generateKey(context, device_id_keystoreAlias)
        publishPubKey = Cryptography.generateKey(context, publisher_keystoreAlias)
    }

    @Test
    fun getPlatformsTest() {
        val response = Publisher.getAvailablePlatforms(context)
        assertTrue(response.isNotEmpty())
    }

    @Test
    fun endToEndCompleteTest() {
        try {
            println("Starting")
            val response = vault.createEntity(context,
                globalPhoneNumber,
                globalCountryCode,
                globalPassword)

            assertTrue(response.requiresOwnershipProof)

            var response1 = vault.createEntity(context,
                globalPhoneNumber,
                globalCountryCode,
                globalPassword,
                "123456")

        } catch(e: StatusRuntimeException) {
            println("Exception code: ${e.status.code.value()}")
            println("Exception code: ${e.status.description}")
            when(e.status.code.value()) {
                3 -> {
                    println("invalid arg - code is ${e.status.code.value()}")
                    throw e
                }
                6 -> { println("already exist - code is ${e.status.code.value()}")}
            }
        } catch(e: Exception) {
            println("Regular exception requested")
        }

        try {
            val response4 = vault.recoverEntityPassword(context,
                globalPhoneNumber,
                globalPassword)

            assertTrue(response4.requiresOwnershipProof)

            val response5 = vault.recoverEntityPassword(context,
                globalPhoneNumber,
                globalPassword,
                "123456")

            val response2 = vault.authenticateEntity(context,
                globalPhoneNumber,
                globalPassword)

            assertTrue(response2.requiresOwnershipProof)

            val response3 = vault.authenticateEntity(context,
                globalPhoneNumber,
                globalPassword,
                "123456")

            val llt = Vault.fetchLongLivedToken(context)
            var response6 = vault.deleteEntity(llt)
        } catch(e: StatusRuntimeException) {
            println("Exception code: ${e.status.code.value()}")
            println("Exception code: ${e.status.description}")
            when(e.status.code.value()) {
                3 -> {
                    println("invalid arg - code is ${e.status.code.value()}")
                    throw e
                }
                6 -> { println("already exist - code is ${e.status.code.value()}")}
            }
        } catch(e: Exception) {
            throw e
        }
    }
}