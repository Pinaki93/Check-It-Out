package dev.pinaki.todoapp.common.model

import androidx.annotation.StringRes

class AlertDialogItem(
    @StringRes val title: Int? = null,
    @StringRes val message: Int? = null,
    val cancelable: Boolean = false,
    val positiveButtonItem: ButtonItem? = null,
    val negativeButtonItem: ButtonItem? = null,
    val neutralButtonItem: ButtonItem? = null
)

class ButtonItem(@StringRes val text: Int, inline val listener: (() -> Boolean)? = null)