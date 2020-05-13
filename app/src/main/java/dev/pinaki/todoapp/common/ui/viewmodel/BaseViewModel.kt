package dev.pinaki.todoapp.common.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.pinaki.todoapp.common.model.AlertDialogItem
import dev.pinaki.todoapp.data.ds.Event

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    protected val _showToast = MutableLiveData<Event<Int>>()
    val showToast: LiveData<Event<Int>> = _showToast

    protected val _showAlertDialog = MutableLiveData<Event<AlertDialogItem>>()
    val showAlertDialog : LiveData<Event<AlertDialogItem>> = _showAlertDialog

}