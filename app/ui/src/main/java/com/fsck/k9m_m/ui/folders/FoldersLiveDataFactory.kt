package com.fsck.k9m_m.ui.folders

import com.fsck.k9m_m.Account
import com.fsck.k9m_m.controller.MessagingController
import com.fsck.k9m_m.mailstore.FolderRepositoryManager

class FoldersLiveDataFactory(
        private val folderRepositoryManager: FolderRepositoryManager,
        private val messagingController: MessagingController
) {
    fun create(account: Account): FoldersLiveData {
        val folderRepository = folderRepositoryManager.getFolderRepository(account)
        return FoldersLiveData(folderRepository, messagingController, account.uuid)
    }
}
