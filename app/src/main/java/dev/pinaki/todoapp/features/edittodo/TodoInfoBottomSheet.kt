package dev.pinaki.todoapp.features.edittodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.ui.bottomsheet.BaseBottomSheetDialogFragment
import dev.pinaki.todoapp.common.util.getAsDisplayString
import dev.pinaki.todoapp.common.util.gone
import dev.pinaki.todoapp.databinding.TodoInfoBinding
import java.util.*

class TodoInfoBottomSheet : BaseBottomSheetDialogFragment() {

    private lateinit var dateCreated: Date
    private lateinit var dateModified: Date
    private var dateCompleted: Date? = null

    private lateinit var binding: TodoInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.run {
            dateCreated = getSerializable(ARG_DATE_CREATED) as Date
            dateModified = getSerializable(ARG_DATE_MODIFIED) as Date
            dateCompleted = getSerializable(ARG_DATE_COMPLETED) as Date?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.layout_todo_info, container, false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDateAdded.text =
            getString(R.string.msg_added_on, dateCreated.getAsDisplayString())
        binding.tvDateModified.text =
            getString(R.string.msg_modified_on, dateModified.getAsDisplayString())

        if (dateCompleted != null) {
            binding.tvDateCompleted.text =
                getString(R.string.msg_completed_on, dateCompleted!!.getAsDisplayString())
        } else {
            binding.tvDateCompleted.gone()
        }
    }

    companion object {
        const val TAG = "TodoInfoBottomSheet"

        private const val ARG_DATE_CREATED = "date_created"
        private const val ARG_DATE_MODIFIED = "date_modified"
        private const val ARG_DATE_COMPLETED = "date_completed"

        fun getInstance(
            dateCreated: Date,
            dateModfied: Date,
            dateCompleted: Date?
        ): TodoInfoBottomSheet {
            return TodoInfoBottomSheet().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATE_CREATED, dateCreated)
                    putSerializable(ARG_DATE_MODIFIED, dateModfied)
                    putSerializable(ARG_DATE_COMPLETED, dateCompleted)
                }
            }
        }
    }
}