package com.fsck.k9m_m.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.core.os.bundleOf

inline fun FragmentActivity.fragmentTransaction(crossinline block: FragmentTransaction.() -> Unit) {
    with(supportFragmentManager.beginTransaction()) {
        block()
        commit()
    }
}

inline fun FragmentActivity.fragmentTransactionWithBackStack(
        name: String? = null,
        crossinline block: FragmentTransaction.() -> Unit
) {
    fragmentTransaction {
        block()
        addToBackStack(name)
    }
}

fun Fragment.withArguments(vararg argumentPairs: Pair<String, Any?>) = apply {
    arguments = bundleOf(*argumentPairs)
}
