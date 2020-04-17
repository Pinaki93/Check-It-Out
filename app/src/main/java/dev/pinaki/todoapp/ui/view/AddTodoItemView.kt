package dev.pinaki.todoapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.databinding.AddTodoBinding
import dev.pinaki.todoapp.util.gone
import dev.pinaki.todoapp.util.showKeyboard
import dev.pinaki.todoapp.util.visible

class AddTodoItemView : ConstraintLayout, CompoundButton.OnCheckedChangeListener,
    View.OnClickListener, TextView.OnEditorActionListener {

    private val binding: AddTodoBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context), R.layout.layout_add_todo_item,
        this, true
    )

    var listener: OnItemInteractionListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {

        binding.cbShowDescription.isChecked = false
        binding.cbShowDescription.setOnCheckedChangeListener(this)

        binding.etTodoItemDescription.gone()

        binding.btnAddTodoItem.setOnClickListener(this)
        binding.etTodoItem.setOnEditorActionListener(this)
        binding.etTodoItemDescription.setOnEditorActionListener(this)
    }

    fun showCollapsedView(shouldShow: Boolean) {
        if (shouldShow) {
            binding.btnAddTodoItem.gone()
            binding.cbShowDescription.gone()
            showDescription(false)
        } else {
            binding.btnAddTodoItem.visible()
            binding.cbShowDescription.visible()
            showDescription(binding.cbShowDescription.isChecked, false)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView?.id == R.id.cb_show_description) {
            showDescription(isChecked)
        }
    }

    private fun showDescription(shouldShow: Boolean, shouldFocus: Boolean = true) {
        if (shouldShow) {
            binding.etTodoItemDescription.visible()
            if (shouldFocus)
                context.showKeyboard(binding.etTodoItemDescription)
        } else {
            binding.etTodoItemDescription.gone()
        }
    }

    fun getItemText(): Pair<String, String> =
        Pair(binding.etTodoItem.text.toString(), binding.etTodoItemDescription.text.toString())

    fun clearText() {
        binding.etTodoItem.text.clear()
        binding.etTodoItemDescription.text.clear()
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_add_todo_item) {
            listener?.onAddTodoItem()
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        listener?.onAddTodoItem()
        return true
    }
}

interface OnItemInteractionListener {
    fun onAddTodoItem()
}