package com.fsck.k9m_m.ui.endtoend

import android.app.PendingIntent
import android.content.Intent
import com.fsck.k9m_m.Account
import com.fsck.k9m_m.autocrypt.AutocryptTransferMessageCreator
import com.fsck.k9m_m.helper.SingleLiveEvent
import com.fsck.k9m_m.mail.Address
import com.fsck.k9m_m.mail.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.openintents.openpgp.util.OpenPgpApi
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AutocryptSetupMessageLiveEvent(val messageCreator: AutocryptTransferMessageCreator) : SingleLiveEvent<AutocryptSetupMessage>() {
    fun loadAutocryptSetupMessageAsync(openPgpApi: OpenPgpApi, account: Account) {
        GlobalScope.launch(Dispatchers.Main) {
            val setupMessage = async {
                loadAutocryptSetupMessage(openPgpApi, account)
            }

            value = setupMessage.await()
        }
    }

    private fun loadAutocryptSetupMessage(openPgpApi: OpenPgpApi, account: Account): AutocryptSetupMessage {
        val keyIds = longArrayOf(account.openPgpKey)
        val address = Address.parse(account.getIdentity(0).email)[0]

        val intent = Intent(OpenPgpApi.ACTION_AUTOCRYPT_KEY_TRANSFER)
        intent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, keyIds)
        val baos = ByteArrayOutputStream()
        val result = openPgpApi.executeApi(intent, null as InputStream?, baos)

        val keyData = baos.toByteArray()
        val pi: PendingIntent = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT)

        val setupMessage = messageCreator.createAutocryptTransferMessage(keyData, address)

        return AutocryptSetupMessage(setupMessage, pi)
    }
}

data class AutocryptSetupMessage(val setupMessage: Message, val showTransferCodePi: PendingIntent)
