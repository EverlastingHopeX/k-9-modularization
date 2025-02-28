package com.fsck.k9m_m

import com.fsck.k9m_m.activity.MessageCompose
import com.fsck.k9m_m.controller.MessagingController
import com.fsck.k9m_m.external.MessageProvider
import com.fsck.k9m_m.ui.ThemeManager
import com.google.android.play.core.splitcompat.SplitCompatApplication
import org.koin.android.ext.android.inject

class   App : SplitCompatApplication() {
    private val messagingController: MessagingController by inject()
    private val messagingListenerProvider: MessagingListenerProvider by inject()
    private val themeManager: ThemeManager by inject()


    override fun onCreate() {
        Core.earlyInit(this)

        super.onCreate()

        DI.start(this, Core.coreModules + uiModules + appModules)

        K9.init(this)
        Core.init(this)
        MessageProvider.init()
        themeManager.init()

        messagingListenerProvider.listeners.forEach { listener ->
            messagingController.addListener(listener)
        }
    }


    companion object {
        val appConfig = AppConfig(
                componentsToDisable = listOf(MessageCompose::class.java)
        )
    }
}
