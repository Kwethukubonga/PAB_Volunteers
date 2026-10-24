package com.kantu.pab_volunteers.ui.safety

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.Concern
import com.kantu.pab_volunteers.data.repository.ConcernRepository
import com.kantu.pab_volunteers.data.repository.UserRepository
import com.kantu.pab_volunteers.databinding.ActivityReportConcernBinding
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.ErrorMessages
import com.kantu.pab_volunteers.utils.Network
import com.kantu.pab_volunteers.utils.OfflineBanner
import com.kantu.pab_volunteers.utils.UiText
import kotlinx.coroutines.launch

/**
 * Lets a volunteer tell Philisa's admins about something that worries them, either about one
 * activity (opened from its details) or in general (opened from Settings). The rules let only
 * admins read it, and it is sent under the volunteer's own account so it can be followed up.
 */
class ReportConcernActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportConcernBinding
    private val concernRepository = ConcernRepository()
    private val userRepository = UserRepository()

    private val activityId: String get() = intent.getStringExtra(Constants.EXTRA_ACTIVITY_ID).orEmpty()
    private val activityTitle: String get() = intent.getStringExtra(EXTRA_ACTIVITY_TITLE).orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportConcernBinding.inflate(layoutInflater)
        setContentView(binding.root)
        OfflineBanner.attach(this, binding.offlineBanner.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.tvAbout.isVisible = activityTitle.isNotBlank()
        binding.tvAbout.text = getString(R.string.concern_about, activityTitle)
        binding.btnSend.setOnClickListener { send() }
    }

    private fun send() {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return
        val message = binding.etMessage.text?.toString().orEmpty()
        ConcernRules.problemWith(message)?.let {
            showMessage(UiText.Res(it))
            return
        }
        if (!Network.isOnline(this)) {
            showMessage(ErrorMessages.OFFLINE)
            return
        }
        setBusy(true)
        lifecycleScope.launch {
            // The name helps the admin follow up; the email stands in if the profile can't be read.
            val name = runCatching { userRepository.getUser(uid)?.fullName }.getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: FirebaseAuthManager.currentUser?.email.orEmpty()
            concernRepository.report(
                Concern(
                    reportedBy = uid,
                    reporterName = name,
                    activityId = activityId,
                    activityTitle = activityTitle,
                    message = message.trim()
                )
            ).onSuccess {
                Toast.makeText(this@ReportConcernActivity, R.string.concern_sent, Toast.LENGTH_LONG).show()
                finish()
            }.onFailure {
                setBusy(false)
                showMessage(ErrorMessages.textFor(it))
            }
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.progress.isVisible = busy
        binding.btnSend.isEnabled = !busy
        binding.etMessage.isEnabled = !busy
    }

    private fun showMessage(message: UiText) {
        binding.tvMessage.text = message.resolve(this)
        binding.tvMessage.isVisible = true
    }

    companion object {
        const val EXTRA_ACTIVITY_TITLE = "extra_activity_title"
    }
}
