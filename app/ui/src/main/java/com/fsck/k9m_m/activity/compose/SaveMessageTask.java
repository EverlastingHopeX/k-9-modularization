package com.fsck.k9m_m.activity.compose;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.fsck.k9m_m.Account;
import com.fsck.k9m_m.activity.MessageCompose;
import com.fsck.k9m_m.controller.MessagingController;
import com.fsck.k9m_m.helper.Contacts;
import com.fsck.k9m_m.mail.Message;

public class SaveMessageTask extends AsyncTask<Void, Void, Void> {
    Context context;
    Account account;
    Contacts contacts;
    Handler handler;
    Message message;
    long draftId;
    String plaintextSubject;
    boolean saveRemotely;

    public SaveMessageTask(Context context, Account account, Contacts contacts,
                           Handler handler, Message message, long draftId, String plaintextSubject, boolean saveRemotely) {
        this.context = context;
        this.account = account;
        this.contacts = contacts;
        this.handler = handler;
        this.message = message;
        this.draftId = draftId;
        this.plaintextSubject = plaintextSubject;
        this.saveRemotely = saveRemotely;
    }

    @Override
    protected Void doInBackground(Void... params) {
        final MessagingController messagingController = MessagingController.getInstance(context);
        Message draftMessage = messagingController.saveDraft(account, message, draftId, plaintextSubject, saveRemotely);
        draftId = messagingController.getId(draftMessage);

        android.os.Message msg = android.os.Message.obtain(handler, MessageCompose.MSG_SAVED_DRAFT, draftId);
        handler.sendMessage(msg);
        return null;
    }
}
