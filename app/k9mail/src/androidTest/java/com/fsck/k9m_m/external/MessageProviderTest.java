package com.fsck.k9m_m.external;


import java.util.List;

import android.database.Cursor;
import android.net.Uri;
import androidx.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.fsck.k9m_m.Account;
import com.fsck.k9m_m.Preferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MessageProviderTest extends ProviderTestCase2 {

    private MockContentResolver mMockResolver;
    private Cursor cursor;

    public MessageProviderTest() {
        super(MessageProvider.class, MessageProvider.AUTHORITY);
    }


    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mMockResolver = getMockContentResolver();
        mContext = getMockContext();
        Preferences preferences = Preferences.getPreferences(getMockContext());
        List<Account> accountList = preferences.getAccounts();
        for (Account account: accountList) {
            preferences.deleteAccount(account);
        }
    }

    @After
    @Override
    public void tearDown() throws Exception {
        if (cursor != null) {
            cursor.close();
        }
        super.tearDown();
    }

    private void createAccount() {
        Preferences preferences = Preferences.getPreferences(getMockContext());
        Account account = preferences.newAccount();
        account.setDescription("TestAccount");
        account.setChipColor(10);
        account.setStoreUri("imap://user@domain.com/");
        preferences.saveAccount(account);
    }

    @Test
    public void query_forAccounts_withNoAccounts_returnsEmptyCursor() {
        cursor = mMockResolver.query(
                Uri.parse("content://" + MessageProvider.AUTHORITY + "/accounts/"),
                null, null, null, null);

        boolean isNotEmpty = cursor.moveToFirst();

        assertFalse(isNotEmpty);
    }

    @Test
    public void query_forAccounts_withAccount_returnsCursorWithData() {
        createAccount();
        cursor = mMockResolver.query(
                Uri.parse("content://" + MessageProvider.AUTHORITY + "/accounts/"),
                null, null, null, null);

        boolean isNotEmpty = cursor.moveToFirst();

        assertTrue(isNotEmpty);
    }

    @Test
    public void query_forAccounts_withAccount_withNoProjection_returnsNumberAndName() {
        createAccount();

        cursor = mMockResolver.query(
                Uri.parse("content://" + MessageProvider.AUTHORITY + "/accounts/"),
                null, null, null, null);
        cursor.moveToFirst();

        assertEquals(2, cursor.getColumnCount());
        assertEquals(0, cursor.getColumnIndex(MessageProvider.AccountColumns.ACCOUNT_NUMBER));
        assertEquals(1, cursor.getColumnIndex(MessageProvider.AccountColumns.ACCOUNT_NAME));
        assertEquals(0, cursor.getInt(0));
        assertEquals("TestAccount", cursor.getString(1));
    }


    @Test
    public void query_forInboxMessages_whenEmpty_returnsEmptyCursor() {
        cursor = mMockResolver.query(
                Uri.parse("content://" + MessageProvider.AUTHORITY + "/inbox_messages/"),
                null, null, null, null);

        boolean isNotEmpty = cursor.moveToFirst();

        assertFalse(isNotEmpty);
    }

    @Test
    public void query_forAccountUnreadMessages_whenNoAccount_returnsEmptyCursor() {
        cursor = mMockResolver.query(
                Uri.parse("content://" + MessageProvider.AUTHORITY + "/account_unread/0"),
                null, null, null, null);

        boolean isNotEmpty = cursor.moveToFirst();

        assertFalse(isNotEmpty);
    }

    @Test
    public void query_forAccountUnreadMessages_whenNoMessages_returns0Unread() {
        createAccount();
        cursor = mMockResolver.query(
                Uri.parse("content://" + MessageProvider.AUTHORITY + "/account_unread/0"),
                null, null, null, null);
        cursor.moveToFirst();

        assertEquals(2, cursor.getColumnCount());
        assertEquals(1, cursor.getColumnIndex(MessageProvider.UnreadColumns.ACCOUNT_NAME));
        assertEquals(0, cursor.getColumnIndex(MessageProvider.UnreadColumns.UNREAD));
        assertEquals(0, cursor.getInt(0));
        assertEquals("TestAccount", cursor.getString(1));
    }
}
