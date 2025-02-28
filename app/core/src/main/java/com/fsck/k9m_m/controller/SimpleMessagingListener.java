
package com.fsck.k9m_m.controller;


import java.util.List;

import android.content.Context;

import com.fsck.k9m_m.Account;
import com.fsck.k9m_m.AccountStats;
import com.fsck.k9m_m.BaseAccount;
import com.fsck.k9m_m.mail.Message;
import com.fsck.k9m_m.mail.Part;
import com.fsck.k9m_m.mailstore.LocalFolder;
import com.fsck.k9m_m.mailstore.LocalMessage;


public abstract class SimpleMessagingListener implements MessagingListener {
    @Override
    public void searchStats(AccountStats stats) {
    }

    @Override
    public void accountStatusChanged(BaseAccount account, AccountStats stats) {
    }

    @Override
    public void accountSizeChanged(Account account, long oldSize, long newSize) {
    }

    @Override
    public void listFoldersStarted(Account account) {
    }

    @Override
    public void listFolders(Account account, List<LocalFolder> folders) {
    }

    @Override
    public void listFoldersFinished(Account account) {
    }

    @Override
    public void listFoldersFailed(Account account, String message) {
    }

    @Override
    public void listLocalMessagesAddMessages(Account account, String folderServerId, List<LocalMessage> messages) {
    }

    @Override
    public void synchronizeMailboxStarted(Account account, String folderServerId, String folderName) {
    }

    @Override
    public void synchronizeMailboxHeadersStarted(Account account, String folderServerId, String folderName) {
    }

    @Override
    public void synchronizeMailboxHeadersProgress(Account account, String folderServerId, int completed, int total) {
    }

    @Override
    public void synchronizeMailboxHeadersFinished(Account account, String folderServerId, int totalMessagesInMailbox,
            int numNewMessages) {
    }

    @Override
    public void synchronizeMailboxProgress(Account account, String folderServerId, int completed, int total) {
    }

    @Override
    public void synchronizeMailboxNewMessage(Account account, String folderServerId, Message message) {
    }

    @Override
    public void synchronizeMailboxRemovedMessage(Account account, String folderServerId, String messageServerId) {
    }

    @Override
    public void synchronizeMailboxFinished(Account account, String folderServerId, int totalMessagesInMailbox,
            int numNewMessages) {
    }

    @Override
    public void synchronizeMailboxFailed(Account account, String folderServerId, String message) {
    }

    @Override
    public void loadMessageRemoteFinished(Account account, String folderServerId, String uid) {
    }

    @Override
    public void loadMessageRemoteFailed(Account account, String folderServerId, String uid, Throwable t) {
    }

    @Override
    public void checkMailStarted(Context context, Account account) {
    }

    @Override
    public void checkMailFinished(Context context, Account account) {
    }

    @Override
    public void sendPendingMessagesStarted(Account account) {
    }

    @Override
    public void sendPendingMessagesCompleted(Account account) {
    }

    @Override
    public void sendPendingMessagesFailed(Account account) {
    }

    @Override
    public void emptyTrashCompleted(Account account) {
    }

    @Override
    public void folderStatusChanged(Account account, String folderServerId, int unreadMessageCount) {
    }

    @Override
    public void systemStatusChanged() {
    }

    @Override
    public void messageDeleted(Account account, String folderServerId, String messageServerId) {
    }

    @Override
    public void messageUidChanged(Account account, String folderServerId, String oldUid, String newUid) {
    }

    @Override
    public void setPushActive(Account account, String folderServerId, boolean enabled) {
    }

    @Override
    public void loadAttachmentFinished(Account account, Message message, Part part) {
    }

    @Override
    public void loadAttachmentFailed(Account account, Message message, Part part, String reason) {
    }

    @Override
    public void pendingCommandStarted(Account account, String commandTitle) {
    }

    @Override
    public void pendingCommandsProcessing(Account account) {
    }

    @Override
    public void pendingCommandCompleted(Account account, String commandTitle) {
    }

    @Override
    public void pendingCommandsFinished(Account account) {
    }

    @Override
    public void remoteSearchStarted(String folder) {
    }

    @Override
    public void remoteSearchServerQueryComplete(String folderServerId, int numResults, int maxResults) {
    }

    @Override
    public void remoteSearchFinished(String folderServerId, int numResults, int maxResults, List<String> extraResults) {
    }

    @Override
    public void remoteSearchFailed(String folderServerId, String err) {
    }

    @Override
    public void enableProgressIndicator(boolean enable) {
    }

    @Override
    public void updateProgress(int progress) {

    }
}
