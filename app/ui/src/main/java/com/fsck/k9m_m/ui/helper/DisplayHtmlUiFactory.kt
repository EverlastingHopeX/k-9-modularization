package com.fsck.k9m_m.ui.helper

import com.fsck.k9m_m.message.html.DisplayHtml

class DisplayHtmlUiFactory(private val htmlSettingsProvider: HtmlSettingsProvider) {
    fun createForMessageView(): DisplayHtml {
        return DisplayHtml(htmlSettingsProvider.createForMessageView())
    }

    fun createForMessageCompose(): DisplayHtml {
        return DisplayHtml(htmlSettingsProvider.createForMessageCompose())
    }
}
