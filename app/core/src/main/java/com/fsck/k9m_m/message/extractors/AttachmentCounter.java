package com.fsck.k9m_m.message.extractors;


import java.util.ArrayList;
import java.util.List;

import com.fsck.k9m_m.mail.Message;
import com.fsck.k9m_m.mail.MessagingException;
import com.fsck.k9m_m.mail.Part;
import com.fsck.k9m_m.mail.internet.MessageExtractor;


public class AttachmentCounter {

    public static AttachmentCounter newInstance() {
        return new AttachmentCounter();
    }

    public int getAttachmentCount(Message message) throws MessagingException {
        List<Part> attachmentParts = new ArrayList<>();
        MessageExtractor.findViewablesAndAttachments(message, null, attachmentParts);

        return attachmentParts.size();
    }
}
