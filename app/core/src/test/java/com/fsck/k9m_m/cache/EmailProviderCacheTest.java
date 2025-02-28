package com.fsck.k9m_m.cache;


import java.util.Collections;
import java.util.UUID;

import android.net.Uri;

import com.fsck.k9m_m.RobolectricTest;
import com.fsck.k9m_m.mailstore.LocalFolder;
import com.fsck.k9m_m.mailstore.LocalMessage;
import com.fsck.k9m_m.provider.EmailProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


public class EmailProviderCacheTest extends RobolectricTest {

    private EmailProviderCache cache;
    @Mock
    private LocalMessage mockLocalMessage;
    @Mock
    private LocalFolder mockLocalMessageFolder;
    private Long localMessageId = 1L;
    private Long localMessageFolderId = 2L;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        EmailProvider.CONTENT_URI = Uri.parse("content://test.provider.email");

        cache = EmailProviderCache.getCache(UUID.randomUUID().toString(), RuntimeEnvironment.application);
        when(mockLocalMessage.getDatabaseId()).thenReturn(localMessageId);
        when(mockLocalMessage.getFolder()).thenReturn(mockLocalMessageFolder);
        when(mockLocalMessageFolder.getDatabaseId()).thenReturn(localMessageFolderId);
    }

    @Test
    public void getCache_returnsDifferentCacheForEachUUID() {
        EmailProviderCache cache = EmailProviderCache.getCache("u001", RuntimeEnvironment.application);
        EmailProviderCache cache2 = EmailProviderCache.getCache("u002", RuntimeEnvironment.application);

        assertNotEquals(cache, cache2);
    }

    @Test
    public void getCache_returnsSameCacheForAUUID() {
        EmailProviderCache cache = EmailProviderCache.getCache("u001", RuntimeEnvironment.application);
        EmailProviderCache cache2 = EmailProviderCache.getCache("u001", RuntimeEnvironment.application);

        assertSame(cache, cache2);
    }

    @Test
    public void getValueForMessage_returnsValueSetForMessage() {
        cache.setValueForMessages(Collections.singletonList(1L), "subject", "Subject");

        String result = cache.getValueForMessage(1L, "subject");

        assertEquals("Subject", result);
    }

    @Test
    public void getValueForUnknownMessage_returnsNull() {
        String result = cache.getValueForMessage(1L, "subject");

        assertNull(result);
    }

    @Test
    public void getValueForUnknownMessage_returnsNullWhenRemoved() {
        cache.setValueForMessages(Collections.singletonList(1L), "subject", "Subject");
        cache.removeValueForMessages(Collections.singletonList(1L), "subject");

        String result = cache.getValueForMessage(1L, "subject");

        assertNull(result);
    }

    @Test
    public void getValueForThread_returnsValueSetForThread() {
        cache.setValueForThreads(Collections.singletonList(1L), "subject", "Subject");

        String result = cache.getValueForThread(1L, "subject");

        assertEquals("Subject", result);
    }

    @Test
    public void getValueForUnknownThread_returnsNull() {
        String result = cache.getValueForThread(1L, "subject");

        assertNull(result);
    }

    @Test
    public void getValueForUnknownThread_returnsNullWhenRemoved() {
        cache.setValueForThreads(Collections.singletonList(1L), "subject", "Subject");
        cache.removeValueForThreads(Collections.singletonList(1L), "subject");

        String result = cache.getValueForThread(1L, "subject");

        assertNull(result);
    }

    @Test
    public void isMessageHidden_returnsTrueForHiddenMessage() {
        cache.hideMessages(Collections.singletonList(mockLocalMessage));

        boolean result = cache.isMessageHidden(localMessageId, localMessageFolderId);

        assertTrue(result);
    }

    @Test
    public void isMessageHidden_returnsFalseForUnknownMessage() {
        boolean result = cache.isMessageHidden(localMessageId, localMessageFolderId);

        assertFalse(result);
    }

    @Test
    public void isMessageHidden_returnsFalseForUnhidenMessage() {
        cache.hideMessages(Collections.singletonList(mockLocalMessage));
        cache.unhideMessages(Collections.singletonList(mockLocalMessage));

        boolean result = cache.isMessageHidden(localMessageId, localMessageFolderId);

        assertFalse(result);
    }

}
