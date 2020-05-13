package dev.pinaki.todoapp.common.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<B : ViewDataBinding> : Fragment() {

    @LayoutRes
    protected abstract fun getLayout(): Int

    protected abstract fun getBinding(inflater: LayoutInflater, layout: Int, parent: ViewGroup?): B

    protected abstract fun initializeViewModels()

    protected abstract fun initializeView()

    protected abstract fun observeData()

    protected abstract fun getToolbarInstance(): Toolbar?

    protected open fun loadData() {

    }

    protected open fun fragmentHasOptionsMenu(): Boolean {
        return false
    }

    private lateinit var binding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(fragmentHasOptionsMenu())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getBinding(inflater = inflater, layout = getLayout(), parent = container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getToolbarInstance()?.let {
            (requireActivity() as AppCompatActivity).setSupportActionBar(getToolbarInstance())
        }

        initializeViewModels()
        initializeView()
        loadData()
        observeData()
    }

    fun getBindingInstance(): B {
        if (!::binding.isInitialized)
            throw IllegalStateException("Attempt to get binding instance before creating it")

        return binding
    }
}