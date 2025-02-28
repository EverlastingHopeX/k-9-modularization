package com.fsck.k9m_m.view

import com.fsck.k9m_m.K9
import com.fsck.k9m_m.ui.Theme
import com.fsck.k9m_m.ui.ThemeManager

class WebViewConfigProvider(private val themeManager: ThemeManager) {
    fun createForMessageView() = createWebViewConfig(themeManager.messageViewTheme)

    fun createForMessageCompose() = createWebViewConfig(themeManager.messageComposeTheme)

    private fun createWebViewConfig(theme: Theme): WebViewConfig {
        return WebViewConfig(
                useDarkMode = theme == Theme.DARK,
                autoFitWidth = K9.isAutoFitWidth,
                textZoom = K9.fontSizes.messageViewContentAsPercent
        )
    }
}
