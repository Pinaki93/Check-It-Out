package dev.pinaki.todoapp.ui.features.todolists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import dev.pinaki.todoapp.R

class AllListsFragment : Fragment() {

    companion object {
        fun newInstance() = AllListsFragment()
    }

    private lateinit var viewModel: AllListsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.all_lists_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AllListsViewModel::class.java)

    }

}
