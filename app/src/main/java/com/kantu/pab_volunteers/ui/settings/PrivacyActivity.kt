package com.kantu.pab_volunteers.ui.settings

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.repository.SignupRepository
import com.kantu.pab_volunteers.data.repository.UserRepository
import com.kantu.pab_volunteers.databinding.ActivityPrivacyBinding
import com.kantu.pab_volunteers.utils.AppMessage
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.UiText
import kotlinx.coroutines.launch

/**
 * What the app keeps about a volunteer and what they can do about it, with a copy of it all
 * as a file (I5). The file is written wherever the person chooses through Android's own save
 * dialog, so the app needs no storage permission and keeps no copy of its own.
 */
class PrivacyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyBinding

    /** Held between asking where to save and being told; cleared either way. */
    private var toWrite: String? = null

    private val chooseWhereToSave = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        val text = toWrite
        toWrite = null
        binding.btnDownloadData.isEnabled = true
        // A null uri means the person backed out of the save dialog, which is not a failure.
        if (uri != null && text != null) write(uri, text)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnDownloadData.setOnClickListener { gatherThenSave() }
    }

    private fun gatherThenSave() {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        binding.btnDownloadData.isEnabled = false
        lifecycleScope.launch {
            try {
                val user = UserRepository().getUser(uid)
                if (user == null) {
                    binding.btnDownloadData.isEnabled = true
                    AppMessage.showOver(this@PrivacyActivity, ErrorMessages.GENERIC)
                    return@launch
                }
                val signups = SignupRepository().getMySignups(uid)
                val now = DateUtils.now()
                toWrite = DataExport.asText(user, signups, now)
                chooseWhereToSave.launch(DataExport.fileName(now))
            } catch (e: Exception) {
                binding.btnDownloadData.isEnabled = true
                AppMessage.showOver(this@PrivacyActivity, ErrorMessages.textFor(e))
            }
        }
    }

    private fun write(uri: Uri, text: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
            AppMessage.showOver(this, UiText.Res(R.string.privacy_download_done))
        } catch (e: Exception) {
            AppMessage.showOver(this, UiText.Res(R.string.privacy_download_failed))
        }
    }
}
