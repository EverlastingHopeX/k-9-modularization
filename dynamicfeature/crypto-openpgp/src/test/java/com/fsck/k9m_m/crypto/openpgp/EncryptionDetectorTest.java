package com.fsck.k9m_m.crypto.openpgp;


import com.fsck.k9m_m.mail.Message;
import com.fsck.k9m_m.message.extractors.TextPartFinder;
import org.junit.Before;
import org.junit.Test;

import static com.fsck.k9m_m.crypto.openpgp.MessageCreationHelper.createMessage;
import static com.fsck.k9m_m.crypto.openpgp.MessageCreationHelper.createMultipartMessage;
import static com.fsck.k9m_m.crypto.openpgp.MessageCreationHelper.createPart;
import static com.fsck.k9m_m.crypto.openpgp.MessageCreationHelper.createTextMessage;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EncryptionDetectorTest {
    private static final String CRLF = "\r\n";


    private TextPartFinder textPartFinder;
    private EncryptionDetector encryptionDetector;


    @Before
    public void setUp() {
        textPartFinder = mock(TextPartFinder.class);

        encryptionDetector = new EncryptionDetector(textPartFinder);
    }

    @Test
    public void isEncrypted_withTextPlain_shouldReturnFalse() {
        Message message = createTextMessage("text/plain", "plain text");

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertFalse(encrypted);
    }

    @Test
    public void isEncrypted_withMultipartEncrypted_shouldReturnTrue() throws Exception {
        Message message = createMultipartMessage("multipart/encrypted",
                createPart("application/octet-stream"), createPart("application/octet-stream"));

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertTrue(encrypted);
    }

    @Test
    public void isEncrypted_withSMimePart_shouldReturnTrue() {
        Message message = createMessage("application/pkcs7-mime");

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertTrue(encrypted);
    }

    @Test
    public void isEncrypted_withMultipartMixedContainingSMimePart_shouldReturnTrue() throws Exception {
        Message message = createMultipartMessage("multipart/mixed",
                createPart("application/pkcs7-mime"), createPart("text/plain"));

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertTrue(encrypted);
    }

    @Test
    public void isEncrypted_withInlinePgp_shouldReturnTrue() {
        Message message = createTextMessage("text/plain", "" +
                "-----BEGIN PGP MESSAGE-----" + CRLF +
                "some encrypted stuff here" + CRLF +
                "-----END PGP MESSAGE-----");
        when(textPartFinder.findFirstTextPart(message)).thenReturn(message);

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertTrue(encrypted);
    }

    @Test
    public void isEncrypted_withPlainTextAndPreambleWithInlinePgp_shouldReturnFalse() {
        Message message = createTextMessage("text/plain", "" +
                "preamble" + CRLF +
                "-----BEGIN PGP MESSAGE-----" + CRLF +
                "some encrypted stuff here" + CRLF +
                "-----END PGP MESSAGE-----" + CRLF +
                "epilogue");
        when(textPartFinder.findFirstTextPart(message)).thenReturn(message);

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertFalse(encrypted);
    }

    @Test
    public void isEncrypted_withQuotedInlinePgp_shouldReturnFalse() {
        Message message = createTextMessage("text/plain", "" +
                "good talk!" + CRLF +
                CRLF +
                "> -----BEGIN PGP MESSAGE-----" + CRLF +
                "> some encrypted stuff here" + CRLF +
                "> -----END PGP MESSAGE-----" + CRLF +
                CRLF +
                "-- " + CRLF +
                "my signature");
        when(textPartFinder.findFirstTextPart(message)).thenReturn(message);

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertFalse(encrypted);
    }
}
