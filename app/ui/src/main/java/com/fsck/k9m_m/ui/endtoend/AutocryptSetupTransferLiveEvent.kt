package com.fsck.k9m_m.ui.endtoend

import android.app.PendingIntent
import com.fsck.k9m_m.Account
import com.fsck.k9m_m.controller.MessagingController
import com.fsck.k9m_m.helper.SingleLiveEvent
import kotlinx.coroutines.*

class AutocryptSetupTransferLiveEvent(
        private val messagingController: MessagingController
) : SingleLiveEvent<AutocryptSetupTransferResult>() {

    fun sendMessageAsync(account: Account, setupMsg: AutocryptSetupMessage) {
        GlobalScope.launch(Dispatchers.Main) {
            val setupMessage = async {
                messagingController.sendMessageBlocking(account, setupMsg.setupMessage)
            }

            delay(2000)

            try {
                setupMessage.await()
                value = AutocryptSetupTransferResult.Success(setupMsg.showTransferCodePi)
            } catch (e: Exception) {
                value = AutocryptSetupTransferResult.Failure(e)
            }
        }
    }
}

sealed class AutocryptSetupTransferResult {
    data class Success(val showTransferCodePi: PendingIntent) : AutocryptSetupTransferResult()
    data class Failure(val exception: Exception) : AutocryptSetupTransferResult()
}
