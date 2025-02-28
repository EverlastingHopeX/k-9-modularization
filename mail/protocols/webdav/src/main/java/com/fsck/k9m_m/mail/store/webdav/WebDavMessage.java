package com.fsck.k9m_m.mail.store.webdav;

import com.fsck.k9m_m.mail.Flag;
import com.fsck.k9m_m.mail.Folder;
import com.fsck.k9m_m.mail.MessagingException;
import com.fsck.k9m_m.mail.internet.MimeMessage;
import timber.log.Timber;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static com.fsck.k9m_m.mail.helper.UrlEncodingHelper.decodeUtf8;
import static com.fsck.k9m_m.mail.helper.UrlEncodingHelper.encodeUtf8;

/**
 * A WebDav Message
 */
public class WebDavMessage extends MimeMessage {
    private String mUrl = "";


    WebDavMessage(String uid, Folder folder) {
        this.mUid = uid;
        this.mFolder = folder;
    }

    public void setUrl(String url) {
        // TODO: This is a not as ugly hack (ie, it will actually work). But it's still horrible
        // XXX: prevent URLs from getting to us that are broken
        if (!(url.toLowerCase(Locale.US).contains("http"))) {
            if (!(url.startsWith("/"))) {
                url = "/" + url;
            }
            url = ((WebDavFolder) mFolder).getUrl() + url;
        }

        String[] urlParts = url.split("/");
        int length = urlParts.length;
        String end = urlParts[length - 1];

        this.mUrl = "";
        url = "";

        /**
         * We have to decode, then encode the URL because Exchange likes to not properly encode all characters
         */
        try {
            end = decodeUtf8(end);
            end = encodeUtf8(end);
            end = end.replaceAll("\\+", "%20");
        } catch (IllegalArgumentException iae) {
            Timber.e(iae, "IllegalArgumentException caught in setUrl: ");
        }

        for (int i = 0; i < length - 1; i++) {
            if (i != 0) {
                url = url + "/" + urlParts[i];
            } else {
                url = urlParts[i];
            }
        }

        url = url + "/" + end;

        this.mUrl = url;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public void setSize(int size) {
        this.mSize = size;
    }

    public void setFlagInternal(Flag flag, boolean set) throws MessagingException {
        super.setFlag(flag, set);
    }

    public void setNewHeaders(ParsedMessageEnvelope envelope) throws MessagingException {
        String[] headers = envelope.getHeaderList();
        Map<String, String> messageHeaders = envelope.getMessageHeaders();
        for (String header : headers) {
            String headerValue = messageHeaders.get(header);
            if (header.equals("Content-Length")) {
                int size = Integer.parseInt(headerValue);
                this.setSize(size);
            }

            if (headerValue != null &&
                    !headerValue.equals("")) {
                this.addHeader(header, headerValue);
            }
        }
    }

    @Override
    public void setFlag(Flag flag, boolean set) throws MessagingException {
        super.setFlag(flag, set);
        mFolder.setFlags(Collections.singletonList(this), Collections.singleton(flag), set);
    }
}
