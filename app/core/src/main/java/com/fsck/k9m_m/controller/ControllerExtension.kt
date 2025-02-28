package com.fsck.k9m_m.controller

import com.fsck.k9m_m.backend.BackendManager

interface ControllerExtension {
    fun init(controller: MessagingController, backendManager: BackendManager, controllerInternals: ControllerInternals)


    interface ControllerInternals {
        fun put(description: String, listener: MessagingListener?, runnable: Runnable)
        fun putBackground(description: String, listener: MessagingListener?, runnable: Runnable)
    }
}
