package com.fsck.k9m_m.job

import com.evernote.android.job.Job
import com.fsck.k9m_m.Preferences
import com.fsck.k9m_m.controller.MessagingController
import com.fsck.k9m_m.service.CoreService
import timber.log.Timber


class MailSyncJob(
        private val messagingController: MessagingController,
        private val preferences: Preferences
) : Job() {

    override fun onRunJob(params: Params): Result {
        if (!CoreService.isBackgroundSyncAllowed()) {
            Timber.d("Background sync is disabled. Skipping mail sync.")
            return Result.SUCCESS
        }

        params.extras.getString(K9JobManager.EXTRA_KEY_ACCOUNT_UUID, null)
                ?.let { accountUuid ->

                    preferences.getAccount(accountUuid)?.let { account ->
                        messagingController.checkMailBlocking(account)
                    }
                }

        return Result.SUCCESS
    }

    companion object {
        const val TAG: String = "MailSyncJob"
    }

}
