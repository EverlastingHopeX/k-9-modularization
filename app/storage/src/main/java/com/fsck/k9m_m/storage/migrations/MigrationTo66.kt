package com.fsck.k9m_m.storage.migrations

import android.database.sqlite.SQLiteDatabase


internal object MigrationTo66 {
    @JvmStatic
    fun addEncryptionTypeColumnToMessagesTable(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE messages ADD encryption_type TEXT")

        db.execSQL("UPDATE messages SET encryption_type = 'openpgp' WHERE preview_type = 'encrypted'")
    }
}
