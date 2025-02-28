package com.fsck.k9m_m.provider;


import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;

import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import com.fsck.k9m_m.Account;
import com.fsck.k9m_m.DI;
import com.fsck.k9m_m.Preferences;
import com.fsck.k9m_m.mail.Message;
import com.fsck.k9m_m.mail.MessagingException;
import com.fsck.k9m_m.mail.internet.MimeMessage;
import com.fsck.k9m_m.mailstore.LocalStoreProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class EmailProviderTest extends ProviderTestCase2<EmailProvider> {
    private MimeMessage message;
    private MimeMessage laterMessage;
    private MimeMessage reply;
    private MimeMessage replyAtSameTime;


    public EmailProviderTest() {
        super(EmailProvider.class, EmailProvider.AUTHORITY);
    }

    private void buildMessages() {
        message = new MimeMessage();
        message.setSubject("Test Subject");
        message.setSentDate(new GregorianCalendar(2016, 1, 2).getTime(), false);
        message.setMessageId("<uid001@email.com>");

        laterMessage = new MimeMessage();
        laterMessage.setSubject("Test Subject2");
        laterMessage.setSentDate(new GregorianCalendar(2016, 1, 3).getTime(), false);

        reply = new MimeMessage();
        reply.setSubject("Re: Test Subject");
        reply.setSentDate(new GregorianCalendar(2016, 1, 3).getTime(), false);
        reply.setMessageId("<uid002@email.com>");
        reply.setInReplyTo("<uid001@email.com>");

        replyAtSameTime = new MimeMessage();
        replyAtSameTime.setSubject("Re: Test Subject");
        replyAtSameTime.setSentDate(new GregorianCalendar(2016, 1, 2).getTime(), false);
        replyAtSameTime.setMessageId("<uid002@email.com>");
        replyAtSameTime.setInReplyTo("<uid001@email.com>");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
        buildMessages();
    }

    @Test
    public void onCreate_shouldReturnTrue() {
        assertNotNull(getProvider());

        boolean returnValue = getProvider().onCreate();

        assertEquals(true, returnValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void query_withInvalidURI_throwsIllegalArgumentException() {
        getProvider().query(
                Uri.parse("content://com.google.www"),
                new String[] {},
                "",
                new String[] {},
                "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void query_forMessagesWithInvalidAccount_throwsIllegalArgumentException() {
        Cursor cursor = getProvider().query(
                Uri.parse("content://" + EmailProvider.AUTHORITY + "/account/1/messages"),
                new String[] {},
                "",
                new String[] {},
                "");

        assertNotNull(cursor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void query_forMessagesWithAccountAndWithoutRequiredFields_throwsIllegalArgumentException() {
        Account account = Preferences.getPreferences(getContext()).newAccount();
        account.getUuid();

        Cursor cursor = getProvider().query(
                Uri.parse("content://" + EmailProvider.AUTHORITY + "/account/" + account.getUuid() + "/messages"),
                new String[] {},
                "",
                new String[] {},
                "");

        assertNotNull(cursor);
        assertTrue(cursor.isAfterLast());
    }

    @Test(expected = SQLException.class) //Handle this better?
    public void query_forMessagesWithAccountAndRequiredFieldsWithNoOrderBy_throwsSQLiteException() {
        Account account = Preferences.getPreferences(getContext()).newAccount();
        account.getUuid();

        Cursor cursor = getProvider().query(
                Uri.parse("content://" + EmailProvider.AUTHORITY + "/account/" + account.getUuid() + "/messages"),
                new String[] {
                        EmailProvider.MessageColumns.ID,
                        EmailProvider.MessageColumns.FOLDER_ID,
                        EmailProvider.ThreadColumns.ROOT
                },
                "",
                new String[] {},
                "");

        assertNotNull(cursor);
        assertTrue(cursor.isAfterLast());
    }

    @Test
    public void query_forMessagesWithEmptyAccountAndRequiredFieldsAndOrderBy_providesEmptyResult() {
        Account account = Preferences.getPreferences(getContext()).newAccount();
        account.getUuid();

        Cursor cursor = getProvider().query(
                Uri.parse("content://" + EmailProvider.AUTHORITY + "/account/" + account.getUuid() + "/messages"),
                new String[] {
                        EmailProvider.MessageColumns.ID,
                        EmailProvider.MessageColumns.FOLDER_ID,
                        EmailProvider.ThreadColumns.ROOT,
                        EmailProvider.MessageColumns.SUBJECT
                },
                "",
                new String[] {},
                EmailProvider.MessageColumns.DATE);

        assertNotNull(cursor);
        assertFalse(cursor.moveToFirst());
    }

    @Test
    public void query_forMessagesWithAccountAndRequiredFieldsAndOrderBy_providesResult() throws MessagingException {
        Account account = Preferences.getPreferences(getContext()).newAccount();
        account.getUuid();
        DI.get(LocalStoreProvider.class).getInstance(account).getFolder("Inbox").appendMessages(Collections.singletonList(message));

        Cursor cursor = getProvider().query(
                Uri.parse("content://" + EmailProvider.AUTHORITY + "/account/" + account.getUuid() + "/messages"),
                new String[] {
                        EmailProvider.MessageColumns.ID,
                        EmailProvider.MessageColumns.FOLDER_ID,
                        EmailProvider.ThreadColumns.ROOT,
                        EmailProvider.MessageColumns.SUBJECT },
                "",
                new String[] {},
                EmailProvider.MessageColumns.DATE);

        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        assertEquals(message.getSubject(), cursor.getString(3));
    }

    @Test
    public void query_forMessagesWithAccountAndRequiredFieldsAndOrderBy_sortsCorrectly() throws MessagingException {
        Account account = Preferences.getPreferences(getContext()).newAccount();
        account.getUuid();
        DI.get(LocalStoreProvider.class).getInstance(account)
                .getFolder("Inbox").appendMessages(Arrays.asList(message, laterMessage));

        Cursor cursor = getProvider().query(
                Uri.parse("content://" + EmailProvider.AUTHORITY + "/account/" + account.getUuid() + "/messages"),
                new String[] {
                        EmailProvider.MessageColumns.ID,
                        EmailProvider.MessageColumns.FOLDER_ID,
                        EmailProvider.ThreadColumns.ROOT,
                        EmailProvider.MessageColumns.SUBJECT
                },
                "",
                new String[] {},
                EmailProvider.MessageColumns.DATE + " DESC");

        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        assertEquals(laterMessage.getSubject(), cursor.getString(3));
        cursor.moveToNext();
        assertEquals(message.getSubject(), cursor.getString(3));
    }

    @Test
    public void query_forThreadedMessages_sortsCorrectly() throws MessagingException {
        Account account = Preferences.getPreferences(getContext()).newAccount();
        account.getUuid();
        DI.get(LocalStoreProvider.class).getInstance(account)
                .getFolder("Inbox").appendMessages(Arrays.asList(message, laterMessage));

        Cursor cursor = getProvider().query(
                Uri.parse("content://" + EmailProvider.AUTHORITY + "/account/" + account.getUuid() +
                        "/messages/threaded"),
                new String[] {
                        EmailProvider.MessageColumns.ID,
                        EmailProvider.MessageColumns.FOLDER_ID,
                        EmailProvider.ThreadColumns.ROOT,
                        EmailProvider.MessageColumns.SUBJECT,
                        EmailProvider.MessageColumns.DATE
                },
                "",
                new String[] {},
                EmailProvider.MessageColumns.DATE + " DESC");
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        assertEquals(laterMessage.getSubject(), cursor.getString(3));
        cursor.moveToNext();
        assertEquals(message.getSubject(), cursor.getString(3));
    }

    @Test
    public void query_forThreadedMessages_showsThreadOfEmailOnce() throws MessagingException {
        Account account = Preferences.getPreferences(getContext()).newAccount();
        account.getUuid();
        DI.get(LocalStoreProvider.class).getInstance(account).getFolder("Inbox").appendMessages(Collections.singletonList(message));
        DI.get(LocalStoreProvider.class).getInstance(account).getFolder("Inbox").appendMessages(Collections.singletonList(reply));

        Cursor cursor = getProvider().query(
                Uri.parse("content://" + EmailProvider.AUTHORITY + "/account/" + account.getUuid() +
                        "/messages/threaded"),
                new String[] {
                        EmailProvider.MessageColumns.ID,
                        EmailProvider.MessageColumns.FOLDER_ID,
                        EmailProvider.ThreadColumns.ROOT,
                        EmailProvider.MessageColumns.SUBJECT,
                        EmailProvider.MessageColumns.DATE,
                        EmailProvider.SpecialColumns.THREAD_COUNT
                },
                "",
                new String[] {},
                EmailProvider.MessageColumns.DATE + " DESC");

        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        assertEquals(2, cursor.getInt(5));
        assertFalse(cursor.moveToNext());
    }

    @Test
    public void query_forThreadedMessages_showsThreadOfEmailWithSameSendTimeOnce() throws MessagingException {
        Account account = Preferences.getPreferences(getContext()).newAccount();
        account.getUuid();
        DI.get(LocalStoreProvider.class).getInstance(account).getFolder("Inbox").appendMessages(Collections.singletonList(message));
        DI.get(LocalStoreProvider.class).getInstance(account).getFolder("Inbox").appendMessages(Collections.singletonList(replyAtSameTime));

        Cursor cursor = getProvider().query(
                Uri.parse("content://" + EmailProvider.AUTHORITY + "/account/" + account.getUuid() +
                        "/messages/threaded"),
                new String[] {
                        EmailProvider.MessageColumns.ID,
                        EmailProvider.MessageColumns.FOLDER_ID,
                        EmailProvider.ThreadColumns.ROOT,
                        EmailProvider.MessageColumns.SUBJECT,
                        EmailProvider.MessageColumns.DATE,
                        EmailProvider.SpecialColumns.THREAD_COUNT
                },
                "",
                new String[] {},
                EmailProvider.MessageColumns.DATE + " DESC");
        
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        assertEquals(2, cursor.getInt(5));
        assertFalse(cursor.moveToNext());
    }

    @Test
    public void query_forAThreadOfMessages_returnsMessage() throws MessagingException {
        Account account = Preferences.getPreferences(getContext()).newAccount();
        account.getUuid();
        Message message = new MimeMessage();
        message.setSubject("Test Subject");
        message.setSentDate(new GregorianCalendar(2016, 1, 2).getTime(), false);
        DI.get(LocalStoreProvider.class).getInstance(account).getFolder("Inbox").appendMessages(Collections.singletonList(message));

        //Now get the thread id we just put in.
        Cursor cursor = getProvider().query(
                Uri.parse("content://" + EmailProvider.AUTHORITY + "/account/" + account.getUuid() + "/messages"),
                new String[] {
                        EmailProvider.MessageColumns.ID,
                        EmailProvider.MessageColumns.FOLDER_ID,
                        EmailProvider.ThreadColumns.ROOT,
                },
                "",
                new String[] {},
                EmailProvider.MessageColumns.DATE);

        assertNotNull(cursor);
        cursor.moveToFirst();
        String threadId = cursor.getString(2);

        //Now check the message is listed under that thread
        Cursor threadCursor = getProvider().query(
                Uri.parse("content://" + EmailProvider.AUTHORITY + "/account/" + account.getUuid() +
                        "/thread/" + threadId),
                new String[] {
                        EmailProvider.MessageColumns.ID,
                        EmailProvider.MessageColumns.FOLDER_ID,
                        EmailProvider.ThreadColumns.ROOT,
                        EmailProvider.MessageColumns.SUBJECT,
                        EmailProvider.MessageColumns.DATE
                },
                "",
                new String[] {},
                EmailProvider.MessageColumns.DATE);

        assertNotNull(threadCursor);
        assertTrue(threadCursor.moveToFirst());
        assertEquals(message.getSubject(), threadCursor.getString(3));
    }
}
