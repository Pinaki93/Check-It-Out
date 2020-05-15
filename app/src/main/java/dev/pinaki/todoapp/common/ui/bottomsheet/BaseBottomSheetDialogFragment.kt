package dev.pinaki.todoapp.common.ui.bottomsheet

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.pinaki.todoapp.R

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val contentView =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

            val bottomSheetBehavior = BottomSheetBehavior.from(contentView)
            bottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        onDialogCancel()
                    }
                }

            })

            if (makeTopCornersRounded()) {
                contentView.setBackgroundResource(R.drawable.bg_top_rouned_corners_16dp)
            }
        }

        dialog.setOnDismissListener {
            onDialogCancel()
        }

        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onDialogCancel()
    }

    open fun makeTopCornersRounded() = false

    open fun onDialogCancel() {

    }
}