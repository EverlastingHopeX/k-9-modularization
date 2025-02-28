package com.fsck.k9m_m.activity

import org.koin.dsl.module.applicationContext

val activityModule = applicationContext {
    bean { ColorChipProvider() }
    bean { MessageLoaderHelperFactory(get(), get()) }
}
