package com.fsck.k9m_m.preferences

import androidx.annotation.CheckResult


interface StoragePersister {
    @CheckResult
    fun loadValues(): Map<String, String>

    fun createStorageEditor(storage: Storage): StorageEditor
}
