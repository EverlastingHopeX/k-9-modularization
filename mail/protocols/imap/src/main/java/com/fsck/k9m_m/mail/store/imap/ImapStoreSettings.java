package com.fsck.k9m_m.mail.store.imap;


import java.util.HashMap;
import java.util.Map;

import com.fsck.k9m_m.mail.AuthType;
import com.fsck.k9m_m.mail.ConnectionSecurity;
import com.fsck.k9m_m.mail.ServerSettings;


/**
 * This class is used to store the decoded contents of an ImapStore URI.
 */
public class ImapStoreSettings extends ServerSettings {
    public static final String AUTODETECT_NAMESPACE_KEY = "autoDetectNamespace";
    public static final String PATH_PREFIX_KEY = "pathPrefix";


    public final boolean autoDetectNamespace;
    public final String pathPrefix;


    public ImapStoreSettings(String host, int port, ConnectionSecurity connectionSecurity,
            AuthType authenticationType, String username, String password, String clientCertificateAlias,
            boolean autodetectNamespace, String pathPrefix) {

        super("imap", host, port, connectionSecurity, authenticationType, username,
                password, clientCertificateAlias);

        this.autoDetectNamespace = autodetectNamespace;
        this.pathPrefix = pathPrefix;
    }

    @Override
    public Map<String, String> getExtra() {
        Map<String, String> extra = new HashMap<>();

        extra.put(AUTODETECT_NAMESPACE_KEY, Boolean.valueOf(autoDetectNamespace).toString());
        putIfNotNull(extra, PATH_PREFIX_KEY, pathPrefix);

        return extra;
    }

    @Override
    public ServerSettings newPassword(String newPassword) {
        return new ImapStoreSettings(host, port, connectionSecurity, authenticationType,
                username, newPassword, clientCertificateAlias, autoDetectNamespace, pathPrefix);
    }
}
