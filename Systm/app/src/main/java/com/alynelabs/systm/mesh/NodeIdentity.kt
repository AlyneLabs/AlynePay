package com.alynelabs.systm.mesh

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.spec.ECGenParameterSpec
import java.util.Base64

/**
 * Handles Node Identity using Ed25519 (or EC as fallback) 
 * and derives the unique 64-bit Node Hash ID.
 */
class NodeIdentity(private val context: Context) {

    private val KEY_ALIAS = "alynenet_identity"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"

    val publicKey: ByteArray
    val nodeId: Long

    init {
        val keyPair = getOrCreateKeyPair()
        publicKey = keyPair.public.encoded
        nodeId = deriveNodeId(publicKey)
    }

    private fun getOrCreateKeyPair(): java.security.KeyPair {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC, 
                ANDROID_KEYSTORE
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
             .setDigests(KeyProperties.DIGEST_SHA256)
             .build()
            
            kpg.initialize(parameterSpec)
            return kpg.generateKeyPair()
        }

        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
        return java.security.KeyPair(keyStore.getCertificate(KEY_ALIAS).publicKey, entry.privateKey)
    }

    private fun deriveNodeId(pubKey: ByteArray): Long {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pubKey)
        // Take the first 8 bytes for a 64-bit ID
        var id = 0L
        for (i in 0 until 8) {
            id = (id shl 8) or (hash[i].toLong() and 0xFF)
        }
        return id
    }

    fun getPublicKeyBase64(): String {
        return Base64.getEncoder().encodeToString(publicKey)
    }
}
