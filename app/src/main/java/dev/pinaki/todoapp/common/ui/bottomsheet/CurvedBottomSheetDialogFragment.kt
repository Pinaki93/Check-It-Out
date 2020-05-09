package dev.pinaki.todoapp.common.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.pinaki.todoapp.R

open class CurvedBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val contentView =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            contentView.setBackgroundResource(R.drawable.bg_top_rouned_corners_16dp)
        }

        return dialog
    }
}