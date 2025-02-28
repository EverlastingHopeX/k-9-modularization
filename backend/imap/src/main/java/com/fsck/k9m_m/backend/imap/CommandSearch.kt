package com.fsck.k9m_m.backend.imap


import com.fsck.k9m_m.mail.Flag
import com.fsck.k9m_m.mail.store.imap.ImapStore


internal class CommandSearch(private val imapStore: ImapStore) {

    fun search(
            folderServerId: String,
            query: String?,
            requiredFlags: Set<Flag>?,
            forbiddenFlags: Set<Flag>?
    ): List<String> {
        val folder = imapStore.getFolder(folderServerId)
        try {
            return folder.search(query, requiredFlags, forbiddenFlags)
                    .sortedWith(UidReverseComparator())
                    .map { it.uid }
        } finally {
            folder.close()
        }
    }
}
