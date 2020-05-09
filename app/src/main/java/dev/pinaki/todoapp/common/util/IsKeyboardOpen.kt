package dev.pinaki.todoapp.common.util

import android.app.Activity
import android.os.Build
import android.view.ViewTreeObserver
import androidx.lifecycle.LiveData

class IsKeyboardOpen(val activity: Activity) : LiveData<Boolean>() {

    private val keyboardOpenCloseListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var lastState: Boolean = activity.isKeyboardOpen()

        override fun onGlobalLayout() {
            val isOpen = activity.isKeyboardOpen()
            if (isOpen == lastState) {
                return
            } else {
                lastState = isOpen
                value = isOpen
            }
        }
    }

    override fun onActive() {
        super.onActive()
        listenToKeyboardChanges(true)
    }

    override fun onInactive() {
        super.onInactive()
        listenToKeyboardChanges(false)
    }

    private fun listenToKeyboardChanges(shouldListen: Boolean) {
        if (shouldListen) {
            activity.getRootView().viewTreeObserver.addOnGlobalLayoutListener(
                keyboardOpenCloseListener
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            activity.getRootView().viewTreeObserver.removeOnGlobalLayoutListener(
                keyboardOpenCloseListener
            )
        }
    }
}