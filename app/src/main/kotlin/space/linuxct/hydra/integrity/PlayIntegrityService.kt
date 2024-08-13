package space.linuxct.hydra.integrity

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityToken
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.playintegrity.v1.PlayIntegrity
import com.google.api.services.playintegrity.v1.PlayIntegrityScopes
import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenRequest
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.GsonBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.linuxct.hydra.BuildConfig
import space.linuxct.hydra.R
import space.linuxct.hydra.eventbus.EventBus
import space.linuxct.hydra.eventbus.PlayIntegrityTokenDecryptedEvent
import java.io.ByteArrayOutputStream
import java.io.IOException

class PlayIntegrityService(context: Context) {
    private val mContext = context
    private val _tag = "PlayIntegrityService"
    private lateinit var integrityTokenProvider: StandardIntegrityTokenProvider
    private var lastKnownGoodResponse: StandardIntegrityToken? = null

    fun performAttestation(shouldShowRawLogs: Boolean, attestationChallenge: String) {
        Log.i(_tag, "Requesting Play Integrity Standard (express) token")

        if (!this::integrityTokenProvider.isInitialized){
            val standardIntegrityManager = IntegrityManagerFactory.createStandard(mContext)
            standardIntegrityManager
                .prepareIntegrityToken(PrepareIntegrityTokenRequest.builder().setCloudProjectNumber(94706239109).build())
                .addOnSuccessListener { tokenProvider ->
                    integrityTokenProvider = tokenProvider
                    requestIntegrityVerdict(shouldShowRawLogs, attestationChallenge)
                }
                .addOnFailureListener { exception ->
                    Log.i(_tag, "Play Integrity API failure")
                    Log.i(_tag, exception.message!!)
                    Log.i(_tag, "--------------------")
                }
            return
        }

        requestIntegrityVerdict(shouldShowRawLogs, attestationChallenge)
    }

    fun performRemediation(remediationType: RemediationTypeEnum, previousActivityContext: Activity) {
        Log.i(_tag, "Starting a Play Integrity Standard remediation\nRemediation type: $remediationType\n")

        if (lastKnownGoodResponse == null) {
            Log.w(_tag, "Perform a Play Integrity Standard request first!")
            return
        }

        val dialogTaskResult = lastKnownGoodResponse!!.showDialog(
            previousActivityContext,
            RemediationTypeEnum.toInt(remediationType)
        )

        dialogTaskResult.addOnSuccessListener { value ->
            previousActivityContext.runOnUiThread {
                Toast.makeText(previousActivityContext, "Remediation OK: ${RemediationResultEnum.fromInt(value)}", Toast.LENGTH_LONG).show()
            }

            Log.i(_tag, "Remediation finished with code ${RemediationResultEnum.fromInt(value)}")
            lastKnownGoodResponse = null
        }

        dialogTaskResult.addOnFailureListener { exception ->
            previousActivityContext.runOnUiThread {
                Toast.makeText(previousActivityContext, "Remediation FAIL: ${exception.message!!}", Toast.LENGTH_LONG).show()
            }

            Log.i(_tag, "Remediation failed to execute with exception ${exception.message} -> $exception")
            lastKnownGoodResponse = null
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun requestIntegrityVerdict(shouldShowRawLogs: Boolean, attestationChallenge: String){
        val requestHash = Base64.encodeToString(getRequestNonce(attestationChallenge), Base64.DEFAULT)

        val integrityTokenResponse: Task<StandardIntegrityToken> =
            integrityTokenProvider.request(
                StandardIntegrityTokenRequest.builder()
                    .setRequestHash(requestHash)
                    .build()
            )

        integrityTokenResponse
            .addOnSuccessListener { response ->
                lastKnownGoodResponse = response
                if (shouldShowRawLogs) {
                    Log.i(_tag, "Encrypted response: ${response.token()}")
                }

                GlobalScope.launch(Dispatchers.IO) {
                    decryptToken(response.token())
                }
            }
            .addOnFailureListener { exception ->
                Log.i(_tag, "Play Integrity API failure")
                Log.i(_tag, exception.message!!)
                Log.i(_tag, "--------------------")
            }
    }

    private suspend fun decryptToken(token: String) {
        withContext(Dispatchers.IO){
            val serviceAccountCredentialsFileStream = mContext.resources.openRawResource(R.raw.hydra_service_account)
            val serviceAccountCredentials = GoogleCredentials.fromStream(serviceAccountCredentialsFileStream)
                .createScoped(listOf(PlayIntegrityScopes.PLAYINTEGRITY))

            // Create the Play Integrity service object
            val playIntegrityService = PlayIntegrity.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory(),
                HttpCredentialsAdapter(serviceAccountCredentials)
            ).setApplicationName("Hydra").build()

            // Decode the integrity token
            val decodeIntegrityTokenRequest = DecodeIntegrityTokenRequest().setIntegrityToken(token)
            val decodeIntegrityTokenResponse = playIntegrityService.v1().decodeIntegrityToken(
                BuildConfig.APPLICATION_ID,
                decodeIntegrityTokenRequest
            ).execute()

            mContext.run {
                val result = decodeIntegrityTokenResponse.tokenPayloadExternal

                val gson = GsonBuilder()
                    .serializeNulls()
                    .serializeSpecialFloatingPointValues()
                    .enableComplexMapKeySerialization()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .setLenient()
                    .create()

                val jsonElement = gson.toJson(result)
                EventBus.publish(PlayIntegrityTokenDecryptedEvent(jsonElement))
                Log.i(_tag, "Decrypted Play Integrity response:\n${jsonElement}")
                Log.i(_tag, "--------------------")
            }
        }
    }

    /**
     * Generates a 16-byte nonce with additional data.
     * The nonce should also include additional information, such as a user id or any other details
     * you wish to bind to this space.linuxct.hydra. Here you can provide a String that is included in the
     * nonce after 24 random bytes. During verification, extract this data again and check it
     * against the request that was made with this nonce.
     */
    private fun getRequestNonce(data: String): ByteArray {
        val byteStream = ByteArrayOutputStream()
        try {
            byteStream.write(data.toByteArray())
        } catch (e: IOException) {
            return ByteArray(0)
        }
        return byteStream.toByteArray()
    }
}