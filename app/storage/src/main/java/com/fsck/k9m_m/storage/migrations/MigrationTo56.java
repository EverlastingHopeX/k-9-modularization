package com.fsck.k9m_m.storage.migrations;

import android.database.sqlite.SQLiteDatabase;


class MigrationTo56 {
    static void cleanUpFtsTable(SQLiteDatabase db) {
        db.execSQL("DELETE FROM messages_fulltext WHERE docid NOT IN (SELECT id FROM messages WHERE deleted = 0)");
    }
}
