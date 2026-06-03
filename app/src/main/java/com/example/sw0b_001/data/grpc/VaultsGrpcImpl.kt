package com.example.sw0b_001.data.grpc

import android.content.Context
import com.example.sw0b_001.R
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import vault.v2.EntityGrpc
import vault.v2.Vault
import java.security.Security

class VaultsGrpcImpl(val context: Context) : AutoCloseable {
    companion object {
        const val clientVaultHandshakeKeystoreAliasStaticKeys =
            "clientVaultHandshakeKeystoreAlias_static_keys"
    }

    private var channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(context.getString(R.string.vault_grpc_url),
            context.getString(R.string.vault_grpc_port).toInt())
        .useTransportSecurity()
        .build()

    private var entityStub: EntityGrpc.EntityBlockingStub = EntityGrpc.newBlockingStub(channel)
    init {
        Security.removeProvider("BC")
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }

    fun deleteEntity() : Vault.DeleteEntityResponse {
        val deleteEntityRequest = Vault.DeleteEntityRequest.newBuilder().build()
        return entityStub
//            .withInterceptors(GrpcClientInterceptor(this, tokenHash))
            .deleteEntity(deleteEntityRequest)
    }

    override fun close() {
        if(!channel.isShutdown) {
            channel.shutdown()
        }
    }
}