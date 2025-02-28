package com.fsck.k9m_m.mail.store.pop3;


import java.util.Collections;

import com.fsck.k9m_m.mail.Flag;
import com.fsck.k9m_m.mail.MessagingException;
import com.fsck.k9m_m.mail.internet.MimeMessage;


public class Pop3Message extends MimeMessage {
    Pop3Message(String uid, Pop3Folder folder) {
        mUid = uid;
        mFolder = folder;
        mSize = -1;
    }

    public void setSize(int size) {
        mSize = size;
    }

    @Override
    public void setFlag(Flag flag, boolean set) throws MessagingException {
        super.setFlag(flag, set);
        mFolder.setFlags(Collections.singletonList(this), Collections.singleton(flag), set);
    }
}
