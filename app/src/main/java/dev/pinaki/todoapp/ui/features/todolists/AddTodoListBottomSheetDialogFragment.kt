package dev.pinaki.todoapp.ui.features.todolists

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.databinding.AddTodoListBinding
import dev.pinaki.todoapp.ui.base.bottomsheet.CurvedBottomSheetDialogFragment
import dev.pinaki.todoapp.util.gone
import dev.pinaki.todoapp.util.showKeyboard
import dev.pinaki.todoapp.util.toast
import dev.pinaki.todoapp.util.visible

class AddTodoListBottomSheetDialogFragment : CurvedBottomSheetDialogFragment(),
    View.OnClickListener, TextView.OnEditorActionListener {

    private lateinit var binding: AddTodoListBinding
    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.layout_add_todo_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddTodoList.setOnClickListener(this)
        binding.cbShowDescription.setOnClickListener(this)

        binding.etListName.setOnEditorActionListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (targetFragment is Listener) {
            targetFragment?.let {
                listener = (it as Listener)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add_todo_list -> {
                onSubmit()
            }

            R.id.cb_show_description -> {
                showDescription(isDescriptionShowing())
            }
        }
    }

    private fun showDescription(shouldShow: Boolean) {
        if (shouldShow) {
            binding.etListDescription.visible()
            showKeyboard(binding.etListDescription)
        } else {
            binding.etListDescription.gone()
            showKeyboard(binding.etListName)
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (v == binding.etListName) {
            if (isDescriptionShowing()) {
                showKeyboard(binding.etListDescription)
            } else {
                onSubmit()
            }
        } else {
            onSubmit()
        }

        return true
    }

    private fun onSubmit() {
        val listName = binding.etListName.text.toString()
        if (listName.isEmpty()) {
            toast(getString(R.string.err_list_name_empty))
            return
        }

        val listDescription =
            if (isDescriptionShowing() && binding.etListDescription.text.toString().isNotEmpty())
                binding.etListDescription.text.toString()
            else
                null

        listener?.onAddTodoList(listName, listDescription)
        dismissAllowingStateLoss()

    }

    private fun isDescriptionShowing() = binding.cbShowDescription.isChecked

    interface Listener {
        fun onAddTodoList(listName: String, listDescription: String?)
    }

    companion object {

        const val TAG = "AddTodoListBS"

        fun show(fragment: Fragment) {
            val instance = AddTodoListBottomSheetDialogFragment()
            instance.setTargetFragment(fragment, 1)

            instance.show(fragment.activity!!.supportFragmentManager, TAG)
        }
    }
}