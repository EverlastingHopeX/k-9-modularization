package com.fsck.k9m_m.ui.settings.account

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner

import com.fsck.k9m_m.Account
import com.fsck.k9m_m.ui.R
import kotlinx.android.synthetic.main.account_spinner_item.view.*

class AccountSelectionSpinner : androidx.appcompat.widget.AppCompatSpinner {
    var selection: Account
        get() = selectedItem as Account
        set(account) {
            selectedAccount = account
            val adapter = adapter as AccountsAdapter
            val adapterPosition = adapter.getPosition(account)
            setSelection(adapterPosition, false)
        }

    private val cachedBackground: Drawable
    private var selectedAccount: Account? = null


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        adapter = AccountsAdapter(context)
        cachedBackground = background
    }

    fun setTitle(title: CharSequence) {
        val adapter = adapter as AccountsAdapter
        adapter.title = title
        adapter.notifyDataSetChanged()
    }

    fun setAccounts(accounts: List<Account>) {
        val adapter = adapter as AccountsAdapter
        adapter.clear()
        adapter.addAll(accounts)

        selectedAccount?.let { selection = it }

        val showAccountSwitcher = accounts.size > 1
        isEnabled = showAccountSwitcher
        background = if (showAccountSwitcher) cachedBackground else null
    }


    internal class AccountsAdapter(context: Context) : ArrayAdapter<Account>(context, 0) {
        var title: CharSequence = ""


        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val account = getItem(position)

            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.account_spinner_item, parent, false)

            return view.apply {
                name.text = title
                if (account != null) {
                    email.text = account.email
                }
            }
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val account = getItem(position)

            val view = convertView
                    ?: LayoutInflater.from(context).inflate(R.layout.account_spinner_dropdown_item, parent, false)

            return view.apply {
                if (account != null) {
                    name.text = account.description
                }
                if (account != null) {
                    email.text = account.email
                }
            }
        }
    }
}
