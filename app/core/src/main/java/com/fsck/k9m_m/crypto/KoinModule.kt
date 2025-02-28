package com.fsck.k9m_m.crypto

import org.koin.dsl.module.applicationContext
import org.openintents.openpgp.OpenPgpApiManager

val openPgpModule = applicationContext {
    factory { params -> OpenPgpApiManager(get(), params["lifecycleOwner"]) }
}