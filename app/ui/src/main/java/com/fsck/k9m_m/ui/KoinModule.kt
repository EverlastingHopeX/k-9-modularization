package com.fsck.k9m_m.ui

import com.fsck.k9m_m.ui.folders.FolderNameFormatter
import com.fsck.k9m_m.ui.folders.FoldersLiveDataFactory
import com.fsck.k9m_m.ui.helper.DisplayHtmlUiFactory
import com.fsck.k9m_m.ui.helper.HtmlSettingsProvider
import com.fsck.k9m_m.ui.helper.HtmlToSpanned
import org.koin.dsl.module.applicationContext

val uiModule = applicationContext {
    bean { FolderNameFormatter(get()) }
    bean { HtmlToSpanned() }
    bean { ThemeManager(get()) }
    bean { HtmlSettingsProvider(get()) }
    bean { DisplayHtmlUiFactory(get()) }
    bean { FoldersLiveDataFactory(get(), get()) }
}
