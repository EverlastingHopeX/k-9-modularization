package com.fsck.k9m_m.mailstore.util;


import java.io.File;
import java.io.IOException;


public interface FileFactory {
    File createFile() throws IOException;
}
