package dev.pinaki.todoapp.ui.features.todos.v2

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.databinding.AddTodoViewBinding
import dev.pinaki.todoapp.util.IsKeyboardOpen

class AddTodoItemView : ConstraintLayout {

    private val binding: AddTodoViewBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context), R.layout.layout_add_todo_item_new,
        this, true
    )

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun init(
        viewModel: AddTodoViewModel,
        isKeyboardOpen: IsKeyboardOpen,
        observer: LifecycleOwner
    ) {
        binding.run {
            this.viewModel = viewModel
            this.isKeyboardOpen = isKeyboardOpen
            this.lifecycleOwner = observer

            executePendingBindings()
        }
    }
}