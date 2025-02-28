package com.fsck.k9m_m.preferences.migrations

import android.database.sqlite.SQLiteDatabase

interface StorageMigrationsHelper {
    fun readValue(db: SQLiteDatabase, key: String): String?
    fun writeValue(db: SQLiteDatabase, key: String, value: String?)
    fun insertValue(db: SQLiteDatabase, key: String, value: String?)
}
