package dev.pinaki.todoapp.features.landing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.ui.fragment.BaseFragment
import dev.pinaki.todoapp.common.util.toast
import dev.pinaki.todoapp.databinding.LandingFragmentBinding
import dev.pinaki.todoapp.features.todolists.TodoListsFragment

class LandingFragment : BaseFragment<LandingFragmentBinding>() {
    override fun getLayout() = R.layout.fragment_landing

    override fun getBinding(
        inflater: LayoutInflater,
        layout: Int,
        parent: ViewGroup?
    ): LandingFragmentBinding {
        return DataBindingUtil.inflate(inflater, layout, parent, false)
    }

    override fun initializeViewModels() {

    }

    override fun initializeView() {
        val binding = getBindingInstance()
        binding.landingBottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_item_lists -> {
                    showTodoLists()
                }

                R.id.menu_item_settings -> {
                    toast("TODO")
                }
            }
            return@setOnNavigationItemSelectedListener false
        }

        showTodoLists()
    }

    private fun showTodoLists() {
        val supportFragmentManager = activity?.supportFragmentManager ?: return

        supportFragmentManager.beginTransaction()
            .replace(R.id.landing_container, TodoListsFragment.newInstance(this))
            .commitAllowingStateLoss()
    }

    override fun observeData() {

    }

    override fun getToolbarInstance(): Toolbar? {
        return null
    }

    companion object {
        fun newInstance(): LandingFragment {
            return LandingFragment()
        }
    }
}