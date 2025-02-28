package com.fsck.k9m_m.mail.store.imap;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.fsck.k9m_m.mail.Body;
import com.fsck.k9m_m.mail.DefaultBodyFactory;
import com.fsck.k9m_m.mail.FetchProfile;
import com.fsck.k9m_m.mail.FetchProfile.Item;
import com.fsck.k9m_m.mail.Flag;
import com.fsck.k9m_m.mail.Folder;
import com.fsck.k9m_m.mail.K9LibRobolectricTestRunner;
import com.fsck.k9m_m.mail.Message;
import com.fsck.k9m_m.mail.MessageRetrievalListener;
import com.fsck.k9m_m.mail.MessagingException;
import com.fsck.k9m_m.mail.Part;
import com.fsck.k9m_m.mail.internet.BinaryTempFileBody;
import com.fsck.k9m_m.mail.internet.MimeHeader;
import com.fsck.k9m_m.mail.store.StoreConfig;
import okio.Buffer;
import org.apache.james.mime4j.util.MimeUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RuntimeEnvironment;

import static com.fsck.k9m_m.mail.Folder.OPEN_MODE_RO;
import static com.fsck.k9m_m.mail.Folder.OPEN_MODE_RW;
import static com.fsck.k9m_m.mail.store.imap.ImapResponseHelper.createImapResponse;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.collections.Sets.newSet;


@RunWith(K9LibRobolectricTestRunner.class)
public class ImapFolderTest {
    private ImapStore imapStore;
    private ImapConnection imapConnection;
    private StoreConfig storeConfig;

    @Before
    public void setUp() throws Exception {
        BinaryTempFileBody.setTempDirectory(RuntimeEnvironment.application.getCacheDir());
        imapStore = mock(ImapStore.class);
        storeConfig = mock(StoreConfig.class);
        when(storeConfig.getInboxFolder()).thenReturn("INBOX");
        when(imapStore.getCombinedPrefix()).thenReturn("");
        when(imapStore.getStoreConfig()).thenReturn(storeConfig);

        imapConnection = mock(ImapConnection.class);
    }

    @Test
    public void open_readWrite_shouldOpenFolder() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);

        imapFolder.open(OPEN_MODE_RW);

        assertTrue(imapFolder.isOpen());
    }

    @Test
    public void open_readOnly_shouldOpenFolder() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);

        imapFolder.open(OPEN_MODE_RO);

        assertTrue(imapFolder.isOpen());
    }

    @Test
    public void open_shouldFetchMessageCount() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);

        imapFolder.open(OPEN_MODE_RW);

        assertEquals(23, imapFolder.getMessageCount());
    }

    @Test
    public void open_readWrite_shouldMakeGetModeReturnReadWrite() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);

        imapFolder.open(OPEN_MODE_RW);

        assertEquals(OPEN_MODE_RW, imapFolder.getMode());
    }

    @Test
    public void open_readOnly_shouldMakeGetModeReturnReadOnly() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);

        imapFolder.open(OPEN_MODE_RO);

        assertEquals(OPEN_MODE_RO, imapFolder.getMode());
    }

    @Test
    public void open_shouldMakeExistReturnTrueWithoutExecutingAdditionalCommands() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);

        imapFolder.open(OPEN_MODE_RW);

        assertTrue(imapFolder.exists());
        verify(imapConnection, times(1)).executeSimpleCommand(anyString());
    }

    @Test
    public void open_calledTwice_shouldReuseSameImapConnection() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        imapFolder.open(OPEN_MODE_RW);

        imapFolder.open(OPEN_MODE_RW);

        verify(imapStore, times(1)).getConnection();
    }

    @Test
    public void open_withConnectionThrowingOnReUse_shouldCreateNewImapConnection() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        imapFolder.open(OPEN_MODE_RW);

        doThrow(IOException.class).when(imapConnection).executeSimpleCommand(Commands.NOOP);
        imapFolder.open(OPEN_MODE_RW);

        verify(imapStore, times(2)).getConnection();
    }

    @Test
    public void open_withIoException_shouldThrowMessagingException() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        doThrow(IOException.class).when(imapConnection).executeSimpleCommand("SELECT \"Folder\"");

        try {
            imapFolder.open(OPEN_MODE_RW);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertNotNull(e.getCause());
            assertEquals(IOException.class, e.getCause().getClass());
        }
    }

    @Test
    public void open_withMessagingException_shouldThrowMessagingException() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        doThrow(MessagingException.class).when(imapConnection).executeSimpleCommand("SELECT \"Folder\"");

        try {
            imapFolder.open(OPEN_MODE_RW);
            fail("Expected exception");
        } catch (MessagingException ignored) {
        }
    }

    @Test
    public void open_withoutExistsResponse_shouldThrowMessagingException() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        List<ImapResponse> selectResponses = asList(
                createImapResponse("* OK [UIDNEXT 57576] Predicted next UID"),
                createImapResponse("2 OK [READ-WRITE] Select completed.")
        );
        when(imapConnection.executeSimpleCommand("SELECT \"Folder\"")).thenReturn(selectResponses);

        try {
            imapFolder.open(OPEN_MODE_RW);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Did not find message count during open", e.getMessage());
        }
    }

    @Test
    public void close_shouldCloseImapFolder() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        imapFolder.open(OPEN_MODE_RW);

        imapFolder.close();

        assertFalse(imapFolder.isOpen());
    }

    @Test
    public void exists_withClosedFolder_shouldOpenConnectionAndIssueStatusCommand() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        imapFolder.exists();

        verify(imapConnection).executeSimpleCommand("STATUS \"Folder\" (UIDVALIDITY)");
    }

    @Test
    public void exists_withoutNegativeImapResponse_shouldReturnTrue() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        boolean folderExists = imapFolder.exists();

        assertTrue(folderExists);
    }

    @Test
    public void exists_withNegativeImapResponse_shouldReturnFalse() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        doThrow(NegativeImapResponseException.class).when(imapConnection)
                .executeSimpleCommand("STATUS \"Folder\" (UIDVALIDITY)");

        boolean folderExists = imapFolder.exists();

        assertFalse(folderExists);
    }

    @Test
    public void create_withClosedFolder_shouldOpenConnectionAndIssueCreateCommand() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        imapFolder.create();

        verify(imapConnection).executeSimpleCommand("CREATE \"Folder\"");
    }

    @Test
    public void create_withoutNegativeImapResponse_shouldReturnTrue() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        boolean success = imapFolder.create();

        assertTrue(success);
    }

    @Test
    public void create_withNegativeImapResponse_shouldReturnFalse() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        doThrow(NegativeImapResponseException.class).when(imapConnection).executeSimpleCommand("CREATE \"Folder\"");

        boolean success = imapFolder.create();

        assertFalse(success);
    }

    @Test
    public void copyMessages_withoutDestinationFolderOfWrongType_shouldThrow() throws Exception {
        ImapFolder sourceFolder = createFolder("Source");
        Folder destinationFolder = mock(Folder.class);
        List<ImapMessage> messages = singletonList(mock(ImapMessage.class));

        try {
            sourceFolder.copyMessages(messages, destinationFolder);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("ImapFolder.copyMessages passed non-ImapFolder", e.getMessage());
        }
    }

    @Test
    public void copyMessages_withEmptyMessageList_shouldReturnNull() throws Exception {
        ImapFolder sourceFolder = createFolder("Source");
        ImapFolder destinationFolder = createFolder("Destination");
        List<ImapMessage> messages = Collections.emptyList();

        Map<String, String> uidMapping = sourceFolder.copyMessages(messages, destinationFolder);

        assertNull(uidMapping);
    }

    @Test
    public void copyMessages_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder sourceFolder = createFolder("Source");
        ImapFolder destinationFolder = createFolder("Destination");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        when(imapStore.getCombinedPrefix()).thenReturn("");
        List<ImapMessage> messages = singletonList(mock(ImapMessage.class));

        try {
            sourceFolder.copyMessages(messages, destinationFolder);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Folder Source is not open.", e.getMessage());
        }
    }

    @Test
    public void copyMessages() throws Exception {
        ImapFolder sourceFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        ImapFolder destinationFolder = createFolder("Destination");
        List<ImapMessage> messages = singletonList(createImapMessage("1"));
        setupCopyResponse("x OK [COPYUID 23 1 101] Success");
        sourceFolder.open(OPEN_MODE_RW);

        Map<String, String> uidMapping = sourceFolder.copyMessages(messages, destinationFolder);

        assertNotNull(uidMapping);
        assertEquals("101", uidMapping.get("1"));
    }

    @Test
    public void moveMessages_shouldCopyMessages() throws Exception {
        ImapFolder sourceFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        ImapFolder destinationFolder = createFolder("Destination");
        List<ImapMessage> messages = singletonList(createImapMessage("1"));
        setupCopyResponse("x OK [COPYUID 23 1 101] Success");
        sourceFolder.open(OPEN_MODE_RW);

        Map<String, String> uidMapping = sourceFolder.moveMessages(messages, destinationFolder);

        assertNotNull(uidMapping);
        assertEquals("101", uidMapping.get("1"));
    }

    @Test
    public void moveMessages_shouldDeleteMessagesFromSourceFolder() throws Exception {
        ImapFolder sourceFolder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        ImapFolder destinationFolder = createFolder("Destination");
        List<ImapMessage> messages = singletonList(createImapMessage("1"));
        sourceFolder.open(OPEN_MODE_RW);

        sourceFolder.moveMessages(messages, destinationFolder);

        assertCommandWithIdsIssued("UID STORE 1 +FLAGS.SILENT (\\Deleted)");
    }

    @Test
    public void moveMessages_withEmptyMessageList_shouldReturnNull() throws Exception {
        ImapFolder sourceFolder = createFolder("Source");
        ImapFolder destinationFolder = createFolder("Destination");
        List<ImapMessage> messages = Collections.emptyList();

        Map<String, String> uidMapping = sourceFolder.moveMessages(messages, destinationFolder);

        assertNull(uidMapping);
    }

    @Test
    public void getUnreadMessageCount_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.getUnreadMessageCount();
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void getUnreadMessageCount_connectionThrowsIOException_shouldThrowMessagingException() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        when(imapConnection.executeSimpleCommand("SEARCH 1:* UNSEEN NOT DELETED")).thenThrow(new IOException());
        folder.open(OPEN_MODE_RW);

        try {
            folder.getUnreadMessageCount();
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("IO Error", e.getMessage());
        }
    }

    @Test
    public void getUnreadMessageCount() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        List<ImapResponse> imapResponses = singletonList(createImapResponse("* SEARCH 1 2 3"));
        when(imapConnection.executeSimpleCommand("SEARCH 1:* UNSEEN NOT DELETED")).thenReturn(imapResponses);
        folder.open(OPEN_MODE_RW);

        int unreadMessageCount = folder.getUnreadMessageCount();

        assertEquals(3, unreadMessageCount);
    }

    @Test
    public void getFlaggedMessageCount_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.getFlaggedMessageCount();
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void getFlaggedMessageCount() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        List<ImapResponse> imapResponses = asList(
                createImapResponse("* SEARCH 1 2"),
                createImapResponse("* SEARCH 23 42")
        );
        when(imapConnection.executeSimpleCommand("SEARCH 1:* FLAGGED NOT DELETED")).thenReturn(imapResponses);
        folder.open(OPEN_MODE_RW);

        int flaggedMessageCount = folder.getFlaggedMessageCount();

        assertEquals(4, flaggedMessageCount);
    }

    @Test
    public void getHighestUid() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        setupUidSearchResponses("* SEARCH 42");
        folder.open(OPEN_MODE_RW);

        long highestUid = folder.getHighestUid();

        assertEquals(42L, highestUid);
    }

    @Test
    public void getHighestUid_imapConnectionThrowsNegativesResponse_shouldReturnMinusOne() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        doThrow(NegativeImapResponseException.class).when(imapConnection).executeSimpleCommand("UID SEARCH *:*");
        folder.open(OPEN_MODE_RW);

        long highestUid = folder.getHighestUid();

        assertEquals(-1L, highestUid);
    }

    @Test
    public void getHighestUid_imapConnectionThrowsIOException_shouldThrowMessagingException() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        doThrow(IOException.class).when(imapConnection).executeSimpleCommand("UID SEARCH *:*");
        folder.open(OPEN_MODE_RW);

        try {
            folder.getHighestUid();
            fail("Expected MessagingException");
        } catch (MessagingException e) {
            assertEquals("IO Error", e.getMessage());
        }
    }

    @Test
    public void getMessages_withoutDateConstraint() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        setupUidSearchResponses("* SEARCH 3", "* SEARCH 5", "* SEARCH 6");
        folder.open(OPEN_MODE_RW);

        List<ImapMessage> messages = folder.getMessages(1, 10, null, null);

        assertNotNull(messages);
        assertEquals(newSet("3", "5", "6"), extractMessageUids(messages));
    }

    @Test
    public void getMessages_withDateConstraint() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        setupUidSearchResponses("* SEARCH 47", "* SEARCH 18");
        folder.open(OPEN_MODE_RW);

        List<ImapMessage> messages = folder.getMessages(1, 10, new Date(1454719826000L), null);

        assertNotNull(messages);
        assertEquals(newSet("18", "47"), extractMessageUids(messages));
    }

    @Test
    public void getMessages_withListener_shouldCallListener() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        setupUidSearchResponses("* SEARCH 99");
        folder.open(OPEN_MODE_RW);
        MessageRetrievalListener<ImapMessage> listener = createMessageRetrievalListener();

        List<ImapMessage> messages = folder.getMessages(1, 10, null, listener);

        ImapMessage message = messages.get(0);
        verify(listener).messageStarted("99", 0, 1);
        verify(listener).messageFinished(message, 0, 1);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void getMessages_withInvalidStartArgument_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");

        try {
            folder.getMessages(0, 10, null, null);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Invalid message set 0 10", e.getMessage());
        }
    }

    @Test
    public void getMessages_withInvalidEndArgument_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");

        try {
            folder.getMessages(10, 0, null, null);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Invalid message set 10 0", e.getMessage());
        }
    }

    @Test
    public void getMessages_withEndArgumentSmallerThanStartArgument_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");

        try {
            folder.getMessages(10, 5, null, null);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Invalid message set 10 5", e.getMessage());
        }
    }

    @Test
    public void getMessages_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.getMessages(1, 5, null, null);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void getMessages_sequenceNumbers_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.getMessages(new HashSet<>(asList(1L, 2L, 5L)), false, null);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void getMessages_sequenceNumbers() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        setupUidSearchResponses("* SEARCH 17", "* SEARCH 18", "* SEARCH 49");
        folder.open(OPEN_MODE_RW);

        List<ImapMessage> messages = folder.getMessages(newSet(1L, 2L, 5L), false, null);

        assertNotNull(messages);
        assertEquals(newSet("17", "18", "49"), extractMessageUids(messages));
    }

    @Test
    public void getMessages_sequenceNumbers_withListener_shouldCallListener() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        setupUidSearchResponses("* SEARCH 99");
        folder.open(OPEN_MODE_RW);
        MessageRetrievalListener<ImapMessage> listener = createMessageRetrievalListener();

        List<ImapMessage> messages = folder.getMessages(singleton(1L), true, listener);

        ImapMessage message = messages.get(0);
        verify(listener).messageStarted("99", 0, 1);
        verify(listener).messageFinished(message, 0, 1);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void getMessagesFromUids_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.getMessagesFromUids(asList("11", "22", "25"));
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void getMessagesFromUids() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        setupUidSearchResponses("* SEARCH 11", "* SEARCH 22", "* SEARCH 25");
        folder.open(OPEN_MODE_RW);

        List<ImapMessage> messages = folder.getMessagesFromUids(asList("11", "22", "25"));

        assertNotNull(messages);
        assertEquals(newSet("11", "22", "25"), extractMessageUids(messages));
    }

    @Test
    public void areMoreMessagesAvailable_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.areMoreMessagesAvailable(10, new Date());
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void areMoreMessagesAvailable_withAdditionalMessages_shouldReturnTrue() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        setupSearchResponses("* SEARCH 42");
        folder.open(OPEN_MODE_RW);

        boolean areMoreMessagesAvailable = folder.areMoreMessagesAvailable(10, null);

        assertTrue(areMoreMessagesAvailable);
    }

    @Test
    public void areMoreMessagesAvailable_withoutAdditionalMessages_shouldReturnFalse() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        setupSearchResponses("1 OK SEARCH completed");
        folder.open(OPEN_MODE_RW);

        boolean areMoreMessagesAvailable = folder.areMoreMessagesAvailable(600, null);

        assertFalse(areMoreMessagesAvailable);
    }

    @Test
    public void areMoreMessagesAvailable_withIndexOfOne_shouldReturnFalseWithoutPerformingSearch() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        folder.open(OPEN_MODE_RW);

        boolean areMoreMessagesAvailable = folder.areMoreMessagesAvailable(1, null);

        assertFalse(areMoreMessagesAvailable);
        //SELECT during OPEN and no more
        verify(imapConnection, times(1)).executeSimpleCommand(anyString());
    }

    @Test
    public void areMoreMessagesAvailable_withoutAdditionalMessages_shouldIssueSearchCommandsUntilAllMessagesSearched()
            throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        setupSearchResponses("1 OK SEARCH Completed");
        folder.open(OPEN_MODE_RW);

        folder.areMoreMessagesAvailable(600, null);

        assertCommandIssued("SEARCH 100:599 NOT DELETED");
        assertCommandIssued("SEARCH 1:99 NOT DELETED");
    }

    @Test
    public void fetch_withNullMessageListArgument_shouldDoNothing() throws Exception {
        ImapFolder folder = createFolder("Folder");
        FetchProfile fetchProfile = createFetchProfile();

        folder.fetch(null, fetchProfile, null);

        verifyNoMoreInteractions(imapStore);
    }

    @Test
    public void fetch_withEmptyMessageListArgument_shouldDoNothing() throws Exception {
        ImapFolder folder = createFolder("Folder");
        FetchProfile fetchProfile = createFetchProfile();

        folder.fetch(Collections.<ImapMessage>emptyList(), fetchProfile, null);

        verifyNoMoreInteractions(imapStore);
    }

    @Test
    public void fetch_withFlagsFetchProfile_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        folder.open(OPEN_MODE_RO);
        when(imapConnection.readResponse(nullable(ImapResponseCallback.class))).thenReturn(createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(Item.FLAGS);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID FLAGS)", false);
    }

    @Test
    public void fetch_withEnvelopeFetchProfile_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        folder.open(OPEN_MODE_RO);
        when(imapConnection.readResponse(nullable(ImapResponseCallback.class))).thenReturn(createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(Item.ENVELOPE);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID INTERNALDATE RFC822.SIZE BODY.PEEK[HEADER.FIELDS " +
                "(date subject from content-type to cc reply-to message-id references in-reply-to X-K9mail-Identity)]" +
                ")", false);
    }

    @Test
    public void fetch_withStructureFetchProfile_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        folder.open(OPEN_MODE_RO);
        when(imapConnection.readResponse(nullable(ImapResponseCallback.class))).thenReturn(createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(Item.STRUCTURE);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODYSTRUCTURE)", false);
    }

    @Test
    public void fetch_withStructureFetchProfile_shouldSetContentType() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        folder.open(OPEN_MODE_RO);
        String bodyStructure = "(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"US-ASCII\") NIL NIL \"7BIT\" 2279 48)";
        when(imapConnection.readResponse(nullable(ImapResponseCallback.class)))
                .thenReturn(createImapResponse("* 1 FETCH (BODYSTRUCTURE "+bodyStructure+" UID 1)"))
                .thenReturn(createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(Item.STRUCTURE);

        folder.fetch(messages, fetchProfile, null);

        verify(messages.get(0)).setHeader(MimeHeader.HEADER_CONTENT_TYPE, "text/plain;\r\n CHARSET=\"US-ASCII\"");
    }

    @Test
    public void fetch_withBodySaneFetchProfile_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        folder.open(OPEN_MODE_RO);
        when(imapConnection.readResponse(nullable(ImapResponseCallback.class))).thenReturn(createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(Item.BODY_SANE);
        when(storeConfig.getMaximumAutoDownloadMessageSize()).thenReturn(4096);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODY.PEEK[]<0.4096>)", false);
    }

    @Test
    public void fetch_withBodySaneFetchProfileAndNoMaximumDownloadSize_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        folder.open(OPEN_MODE_RO);
        when(imapConnection.readResponse(nullable(ImapResponseCallback.class))).thenReturn(createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(Item.BODY_SANE);
        when(storeConfig.getMaximumAutoDownloadMessageSize()).thenReturn(0);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODY.PEEK[])", false);
    }

    @Test
    public void fetch_withBodyFetchProfileAndNoMaximumDownloadSize_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        folder.open(OPEN_MODE_RO);
        when(imapConnection.readResponse(nullable(ImapResponseCallback.class))).thenReturn(createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(Item.BODY);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODY.PEEK[])", false);
    }

    @Test
    public void fetch_withFlagsFetchProfile_shouldSetFlags() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        folder.open(OPEN_MODE_RO);
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(Item.FLAGS);
        when(imapConnection.readResponse(nullable(ImapResponseCallback.class)))
                .thenReturn(createImapResponse("* 1 FETCH (FLAGS (\\Seen) UID 1)"))
                .thenReturn(createImapResponse("x OK"));

        folder.fetch(messages, fetchProfile, null);

        ImapMessage imapMessage = messages.get(0);
        verify(imapMessage).setFlagInternal(Flag.SEEN, true);
    }

    @Test
    public void fetchPart_withTextSection_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        when(storeConfig.getMaximumAutoDownloadMessageSize()).thenReturn(4096);
        folder.open(OPEN_MODE_RO);
        ImapMessage message = createImapMessage("1");
        Part part = createPart("TEXT");
        when(imapConnection.readResponse(nullable(ImapResponseCallback.class))).thenReturn(createImapResponse("x OK"));

        folder.fetchPart(message, part, null, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODY.PEEK[TEXT]<0.4096>)", false);
    }

    @Test
    public void fetchPart_withNonTextSection_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        folder.open(OPEN_MODE_RO);
        ImapMessage message = createImapMessage("1");
        Part part = createPart("1.1");
        when(imapConnection.readResponse(nullable(ImapResponseCallback.class))).thenReturn(createImapResponse("x OK"));

        folder.fetchPart(message, part, null, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODY.PEEK[1.1])", false);
    }

    @Test
    public void fetchPart_withTextSection_shouldProcessImapResponses() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        folder.open(OPEN_MODE_RO);
        ImapMessage message = createImapMessage("1");
        Part part = createPlainTextPart("1.1");
        setupSingleFetchResponseToCallback();

        folder.fetchPart(message, part, null, new DefaultBodyFactory());

        ArgumentCaptor<Body> bodyArgumentCaptor = ArgumentCaptor.forClass(Body.class);
        verify(part).setBody(bodyArgumentCaptor.capture());
        Body body = bodyArgumentCaptor.getValue();
        Buffer buffer = new Buffer();
        body.writeTo(buffer.outputStream());
        assertEquals("text", buffer.readUtf8());
    }

    @Test
    public void appendMessages_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        folder.open(OPEN_MODE_RW);
        List<ImapMessage> messages = createImapMessages("1");
        when(imapConnection.readResponse()).thenReturn(createImapResponse("x OK [APPENDUID 1 23]"));

        folder.appendMessages(messages);

        verify(imapConnection).sendCommand("APPEND \"Folder\" () {0}", false);
    }

    @Test
    public void getUidFromMessageId_withMessageIdHeader_shouldIssueUidSearchCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        folder.open(OPEN_MODE_RW);
        setupUidSearchResponses("1 OK SEARCH Completed");

        folder.getUidFromMessageId("<00000000.0000000@example.org>");

        assertCommandIssued("UID SEARCH HEADER MESSAGE-ID \"<00000000.0000000@example.org>\"");
    }

    @Test
    public void getUidFromMessageId() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        folder.open(OPEN_MODE_RW);
        setupUidSearchResponses("* SEARCH 23");

        String uid = folder.getUidFromMessageId("<00000000.0000000@example.org>");

        assertEquals("23", uid);
    }

    @Test
    public void expunge_shouldIssueExpungeCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);

        folder.expunge();

        verify(imapConnection).executeSimpleCommand("EXPUNGE");
    }

    @Test
    public void expungeUids_withUidPlus_shouldIssueUidExpungeCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        when(imapConnection.isUidPlusCapable()).thenReturn(true);

        folder.expungeUids(singletonList("1"));

        assertCommandWithIdsIssued("UID EXPUNGE 1");
    }

    @Test
    public void expungeUids_withoutUidPlus_shouldIssueExpungeCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        when(imapConnection.isUidPlusCapable()).thenReturn(false);

        folder.expungeUids(singletonList("1"));

        verify(imapConnection).executeSimpleCommand("EXPUNGE");
    }

    @Test
    public void setFlags_shouldIssueUidStoreCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);

        folder.setFlags(newSet(Flag.SEEN), true);

        assertCommandIssued("UID STORE 1:* +FLAGS.SILENT (\\Seen)");
    }

    @Test
    public void getNewPushState_withNewerUid_shouldReturnNewPushState() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        ImapMessage message = createImapMessage("2");

        String newPushState = folder.getNewPushState("uidNext=2", message);

        assertEquals("uidNext=3", newPushState);
    }

    @Test
    public void getNewPushState_withoutNewerUid_shouldReturnNull() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RW);
        ImapMessage message = createImapMessage("1");

        String newPushState = folder.getNewPushState("uidNext=2", message);

        assertNull(newPushState);
    }

    @Test
    public void search_withFullTextSearchEnabled_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        when(storeConfig.isAllowRemoteSearch()).thenReturn(true);
        when(storeConfig.isRemoteSearchFullText()).thenReturn(true);
        setupUidSearchResponses("1 OK SEARCH completed");

        folder.search("query", newSet(Flag.SEEN), Collections.<Flag>emptySet());

        assertCommandIssued("UID SEARCH TEXT \"query\" SEEN");
    }

    @Test
    public void search_withFullTextSearchDisabled_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(OPEN_MODE_RO);
        when(storeConfig.isAllowRemoteSearch()).thenReturn(true);
        when(storeConfig.isRemoteSearchFullText()).thenReturn(false);
        setupUidSearchResponses("1 OK SEARCH completed");

        folder.search("query", Collections.<Flag>emptySet(), Collections.<Flag>emptySet());

        assertCommandIssued("UID SEARCH OR SUBJECT \"query\" FROM \"query\"");
    }

    @Test
    public void search_withRemoteSearchDisabled_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(storeConfig.isAllowRemoteSearch()).thenReturn(false);

        try {
            folder.search("query", Collections.<Flag>emptySet(), Collections.<Flag>emptySet());
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Your settings do not allow remote searching of this account", e.getMessage());
        }
    }

    @Test
    public void getMessageByUid_returnsNewImapMessageWithUidInFolder() throws Exception {
        ImapFolder folder = createFolder("Folder");

        ImapMessage message = folder.getMessage("uid");

        assertEquals("uid", message.getUid());
        assertEquals(folder, message.getFolder());
    }

    private Part createPlainTextPart(String serverExtra) {
        Part part = createPart(serverExtra);
        when(part.getHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING)).thenReturn(
                new String[] { MimeUtil.ENC_7BIT }
        );
        when(part.getHeader(MimeHeader.HEADER_CONTENT_TYPE)).thenReturn(
                new String[] { "text/plain" }
        );
        return part;
    }

    private void setupSingleFetchResponseToCallback() throws IOException {
        when(imapConnection.readResponse(nullable(ImapResponseCallback.class)))
                .thenAnswer(new Answer<ImapResponse>() {
                    @Override
                    public ImapResponse answer(InvocationOnMock invocation) throws Throwable {
                        ImapResponseCallback callback = (ImapResponseCallback) invocation.getArguments()[0];
                        return buildImapFetchResponse(callback);
                    }
                })
                .thenAnswer(new Answer<ImapResponse>() {
                    @Override
                    public ImapResponse answer(InvocationOnMock invocation) throws Throwable {
                        ImapResponseCallback callback = (ImapResponseCallback) invocation.getArguments()[0];
                        return ImapResponse.newTaggedResponse(callback, "TAG");
                    }
                });
    }

    private ImapResponse buildImapFetchResponse(ImapResponseCallback callback) {
        ImapResponse response = ImapResponse.newContinuationRequest(callback);
        response.add("1");
        response.add("FETCH");
        ImapList fetchList = new ImapList();
        fetchList.add("UID");
        fetchList.add("1");
        fetchList.add("BODY");
        fetchList.add("1.1");
        fetchList.add("text");
        response.add(fetchList);
        return response;
    }

    private Set<String> extractMessageUids(List<ImapMessage> messages) {
        Set<String> result = new HashSet<>();
        for (Message message : messages) {
            result.add(message.getUid());
        }

        return result;
    }

    private ImapFolder createFolder(String folderName) {
        return new ImapFolder(imapStore, folderName, FolderNameCodec.newInstance());
    }

    private ImapMessage createImapMessage(String uid) {
        ImapMessage message = mock(ImapMessage.class);
        when(message.getUid()).thenReturn(uid);

        return message;
    }

    private List<ImapMessage> createImapMessages(String... uids) {
        List<ImapMessage> imapMessages = new ArrayList<>(uids.length);

        for (String uid : uids) {
            ImapMessage imapMessage = createImapMessage(uid);
            imapMessages.add(imapMessage);
        }

        return imapMessages;
    }

    private Part createPart(String serverExtra) {
        Part part = mock(Part.class);
        when(part.getServerExtra()).thenReturn(serverExtra);

        return part;
    }

    private FetchProfile createFetchProfile(Item... items) {
        FetchProfile fetchProfile = new FetchProfile();
        Collections.addAll(fetchProfile, items);

        return fetchProfile;
    }

    @SuppressWarnings("unchecked")
    private MessageRetrievalListener<ImapMessage> createMessageRetrievalListener() {
        return mock(MessageRetrievalListener.class);
    }

    private void prepareImapFolderForOpen(int openMode) throws MessagingException, IOException {
        when(imapStore.getConnection()).thenReturn(imapConnection);
        List<ImapResponse> imapResponses = asList(
                createImapResponse("* FLAGS (\\Answered \\Flagged \\Deleted \\Seen \\Draft NonJunk $MDNSent)"),
                createImapResponse("* OK [PERMANENTFLAGS (\\Answered \\Flagged \\Deleted \\Seen \\Draft NonJunk " +
                        "$MDNSent \\*)] Flags permitted."),
                createImapResponse("* 23 EXISTS"),
                createImapResponse("* 0 RECENT"),
                createImapResponse("* OK [UIDVALIDITY 1125022061] UIDs valid"),
                createImapResponse("* OK [UIDNEXT 57576] Predicted next UID"),
                (openMode == OPEN_MODE_RW) ?
                        createImapResponse("2 OK [READ-WRITE] Select completed.") :
                        createImapResponse("2 OK [READ-ONLY] Examine completed.")
        );

        if (openMode == OPEN_MODE_RW) {
            when(imapConnection.executeSimpleCommand("SELECT \"Folder\"")).thenReturn(imapResponses);
        } else {
            when(imapConnection.executeSimpleCommand("EXAMINE \"Folder\"")).thenReturn(imapResponses);
        }
    }

    private void assertCheckOpenErrorMessage(String folderName, MessagingException e) {
        assertEquals("Folder " + folderName + " is not open.", e.getMessage());
    }

    @SuppressWarnings("unchecked")
    private void assertCommandWithIdsIssued(String expectedCommand) throws MessagingException, IOException {
        ArgumentCaptor<String> commandPrefixCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> commandSuffixCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Set> commandUidsCaptor = ArgumentCaptor.forClass(Set.class);
        verify(imapConnection, atLeastOnce()).executeCommandWithIdSet(
                commandPrefixCaptor.capture(), commandSuffixCaptor.capture(), commandUidsCaptor.capture());

        List<String> commandPrefixes = commandPrefixCaptor.getAllValues();
        List<String> commandSuffixes = commandSuffixCaptor.getAllValues();
        List<Set> commandUids = commandUidsCaptor.getAllValues();

        for (int i = 0, end = commandPrefixes.size(); i < end; i++) {
            String command = commandPrefixes.get(i) +
                    " " + ImapUtility.join(",", commandUids.get(i)) +
                    ((commandSuffixes.get(i).length() == 0) ? "" : " " + commandSuffixes.get(i));
            if (command.equals(expectedCommand)) {
                return;
            }
        }

        fail("Expected IMAP command not issued: " + expectedCommand);
    }

    private void assertCommandIssued(String expectedCommand) throws MessagingException, IOException {
        verify(imapConnection, atLeastOnce()).executeSimpleCommand(expectedCommand);
    }

    private void setupUidSearchResponses(String... responses) throws MessagingException, IOException {
        List<ImapResponse> imapResponses = new ArrayList<>(responses.length);
        for (String response : responses) {
            imapResponses.add(createImapResponse(response));
        }

        when(imapConnection.executeSimpleCommand(startsWith("UID SEARCH"))).thenReturn(imapResponses);
        when(imapConnection.executeCommandWithIdSet(startsWith("UID SEARCH"), anyString(), anySetOf(Long.class)))
                .thenReturn(imapResponses);
    }

    private void setupSearchResponses(String... responses) throws MessagingException, IOException {
        List<ImapResponse> imapResponses = new ArrayList<>(responses.length);
        for (String response : responses) {
            imapResponses.add(createImapResponse(response));
        }

        when(imapConnection.executeSimpleCommand(startsWith("SEARCH"))).thenReturn(imapResponses);
    }

    private void setupCopyResponse(String response) throws MessagingException, IOException {
        List<ImapResponse> imapResponses = singletonList(createImapResponse(response));
        when(imapConnection.executeCommandWithIdSet(eq(Commands.UID_COPY), anyString(), anySetOf(Long.class)))
                .thenReturn(imapResponses);
    }
}
