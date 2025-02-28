package com.fsck.k9m_m.mail.store.webdav;


import java.util.HashMap;
import java.util.Map;

import com.fsck.k9m_m.mail.AuthType;
import com.fsck.k9m_m.mail.ConnectionSecurity;
import com.fsck.k9m_m.mail.ServerSettings;


/**
 * This class is used to store the decoded contents of an WebDavStore URI.
 */
public class WebDavStoreSettings extends ServerSettings {
    public static final String ALIAS_KEY = "alias";
    public static final String PATH_KEY = "path";
    public static final String AUTH_PATH_KEY = "authPath";
    public static final String MAILBOX_PATH_KEY = "mailboxPath";

    public final String alias;
    public final String path;
    public final String authPath;
    public final String mailboxPath;

    public WebDavStoreSettings(String host, int port, ConnectionSecurity connectionSecurity,
            AuthType authenticationType, String username, String password, String clientCertificateAlias, String alias,
            String path, String authPath, String mailboxPath) {
        super("webdav", host, port, connectionSecurity, authenticationType, username,
                password, clientCertificateAlias);
        this.alias = alias;
        this.path = path;
        this.authPath = authPath;
        this.mailboxPath = mailboxPath;
    }

    @Override
    public Map<String, String> getExtra() {
        Map<String, String> extra = new HashMap<>();
        putIfNotNull(extra, ALIAS_KEY, alias);
        putIfNotNull(extra, PATH_KEY, path);
        putIfNotNull(extra, AUTH_PATH_KEY, authPath);
        putIfNotNull(extra, MAILBOX_PATH_KEY, mailboxPath);
        return extra;
    }

    @Override
    public ServerSettings newPassword(String newPassword) {
        return new WebDavStoreSettings(host, port, connectionSecurity, authenticationType,
                username, newPassword, clientCertificateAlias, alias, path, authPath, mailboxPath);
    }
}
