package com.fsck.k9m_m.message.html

class HtmlProcessorFactory(
        private val htmlSanitizer: HtmlSanitizer,
        private val displayHtmlFactory: DisplayHtmlFactory
) {
    fun create(settings: HtmlSettings): HtmlProcessor {
        val displayHtml = displayHtmlFactory.create(settings)
        return HtmlProcessor(htmlSanitizer, displayHtml)
    }
}
