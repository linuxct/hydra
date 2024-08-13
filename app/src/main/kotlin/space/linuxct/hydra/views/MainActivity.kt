package space.linuxct.hydra.views

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import space.linuxct.hydra.R
import space.linuxct.hydra.eventbus.EventBus
import space.linuxct.hydra.eventbus.PlayIntegrityTokenDecryptedEvent
import space.linuxct.hydra.integrity.PlayIntegrityService
import space.linuxct.hydra.integrity.RemediationTypeEnum

class MainActivity : AppCompatActivity() {
    private lateinit var playIntegrityService: PlayIntegrityService

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playIntegrityService = PlayIntegrityService(this)
        GlobalScope.launch(Dispatchers.IO) {
            subscribeToTokenDecrypted()
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            Toast.makeText(this, "Calling Play Integrity API...", Toast.LENGTH_LONG).show()
            playIntegrityService.performAttestation(false, "AttestationTestSample: " + System.currentTimeMillis())
        }
    }

    private suspend fun subscribeToTokenDecrypted(){
        EventBus.subscribe<PlayIntegrityTokenDecryptedEvent> {
            runOnUiThread {
                showPlayIntegrityResponseDialog(this@MainActivity, layoutInflater, it.data) {
                    onPlayIntegrityDialogOk()
                }
            }
        }
    }

    private fun onPlayIntegrityDialogOk(){
        showRemediationDialog(this@MainActivity, layoutInflater) { remediation ->
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Starting remediation...",
                    Toast.LENGTH_LONG
                ).show()
            }
            playIntegrityService.performRemediation(remediation, this@MainActivity)
        }
    }

    private fun showPlayIntegrityResponseDialog(
        context: Context,
        layoutInflater: LayoutInflater,
        dataToShow: String,
        onOkClicked: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.play_integrity_result_dialog, null)
        val resultTextView = view.findViewById<TextView>(R.id.play_integrity_result)
        resultTextView.text = dataToShow

        builder.setView(view)
            .setPositiveButton("Continue") { dialog, _ ->
                onOkClicked()
                dialog.dismiss()
            }
            .show()
    }

    private fun showRemediationDialog(
        context: Context,
        layoutInflater: LayoutInflater,
        onOkClicked: (RemediationTypeEnum) -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.remediation_dialog, null)
        val spinner = view.findViewById<Spinner>(R.id.remediation_spinner)

        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            RemediationTypeEnum.values()
        )
        spinner.adapter = adapter

        builder.setView(view)
            .setPositiveButton("OK") { dialog, _ ->
                val selectedRemediation = spinner.selectedItem as RemediationTypeEnum
                onOkClicked(selectedRemediation)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}