package com.fsck.k9m_m.storage.migrations;


import android.database.sqlite.SQLiteDatabase;

import com.fsck.k9m_m.mailstore.LocalStore;
import com.fsck.k9m_m.mailstore.MigrationsHelper;


public class Migrations {
    @SuppressWarnings("fallthrough")
    public static void upgradeDatabase(SQLiteDatabase db, MigrationsHelper migrationsHelper) {
        boolean shouldBuildFtsTable = false;
        switch (db.getVersion()) {
            case 29:
                MigrationTo30.addDeletedColumn(db);
            case 30:
                MigrationTo31.changeMsgFolderIdDeletedDateIndex(db);
            case 31:
                MigrationTo32.updateDeletedColumnFromFlags(db);
            case 32:
                MigrationTo33.addPreviewColumn(db);
            case 33:
                MigrationTo34.addFlaggedCountColumn(db);
            case 34:
                MigrationTo35.updateRemoveXNoSeenInfoFlag(db);
            case 35:
                MigrationTo36.addAttachmentsContentIdColumn(db);
            case 36:
                MigrationTo37.addAttachmentsContentDispositionColumn(db);
            case 37:
                // Database version 38 is solely to prune cached attachments now that we clear them better
            case 38:
                MigrationTo39.headersPruneOrphans(db);
            case 39:
                MigrationTo40.addMimeTypeColumn(db);
            case 40:
                MigrationTo41.db41FoldersAddClassColumns(db);
                MigrationTo41.db41UpdateFolderMetadata(db, migrationsHelper);
            case 41:
                boolean notUpdatingFromEarlierThan41 = db.getVersion() == 41;
                if (notUpdatingFromEarlierThan41) {
                    MigrationTo42.from41MoveFolderPreferences(migrationsHelper);
                }
            case 42:
                MigrationTo43.fixOutboxFolders(db, migrationsHelper);
            case 43:
                MigrationTo44.addMessagesThreadingColumns(db);
            case 44:
                MigrationTo45.changeThreadingIndexes(db);
            case 45:
                MigrationTo46.addMessagesFlagColumns(db, migrationsHelper);
            case 46:
                MigrationTo47.createThreadsTable(db);
            case 47:
                MigrationTo48.updateThreadsSetRootWhereNull(db);
            case 48:
                MigrationTo49.createMsgCompositeIndex(db);
            case 49:
                MigrationTo50.foldersAddNotifyClassColumn(db, migrationsHelper);
            case 50:
                MigrationTo51.db51MigrateMessageFormat(db, migrationsHelper);
            case 51:
                MigrationTo52.addMoreMessagesColumnToFoldersTable(db);
            case 52:
                MigrationTo53.removeNullValuesFromEmptyColumnInMessagesTable(db);
            case 53:
                MigrationTo54.addPreviewTypeColumn(db);
            case 54:
                MigrationTo55.createFtsSearchTable(db);
                shouldBuildFtsTable = true;
            case 55:
                MigrationTo56.cleanUpFtsTable(db);
            case 56:
                MigrationTo57.fixDataLocationForMultipartParts(db);
            case 57:
                MigrationTo58.cleanUpOrphanedData(db);
                MigrationTo58.createDeleteMessageTrigger(db);
            case 58:
                MigrationTo59.addMissingIndexes(db);
            case 59:
                MigrationTo60.migratePendingCommands(db);
            case 60:
                MigrationTo61.removeErrorsFolder(db);
            case 61:
                MigrationTo62.addServerIdColumnToFoldersTable(db);
            case 63:
                MigrationTo64.addExtraValuesTables(db);
            case 64:
                MigrationTo65.addLocalOnlyColumnToFoldersTable(db, migrationsHelper);
            case 65:
                MigrationTo66.addEncryptionTypeColumnToMessagesTable(db);
            case 66:
                MigrationTo67.addTypeColumnToFoldersTable(db, migrationsHelper);
            case 67:
                MigrationTo68.addOutboxStateTable(db);
            case 68:
                new MigrationTo69(db).createPendingDelete();
        }

        if (shouldBuildFtsTable) {
            buildFtsTable(db, migrationsHelper);
        }
    }

    private static void buildFtsTable(SQLiteDatabase db, MigrationsHelper migrationsHelper) {
        LocalStore localStore = migrationsHelper.getLocalStore();
        FullTextIndexer fullTextIndexer = new FullTextIndexer(localStore, db);
        fullTextIndexer.indexAllMessages();
    }
}
