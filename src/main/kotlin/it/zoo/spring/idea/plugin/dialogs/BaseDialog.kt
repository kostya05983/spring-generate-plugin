package it.zoo.spring.idea.plugin.dialogs

import com.intellij.openapi.ui.DialogWrapper

abstract class BaseDialog : DialogWrapper() {

    fun showDialog() {
        init()
        show()
    }

    fun waitForInput() {
        if (super.isOK()) {
            return
        }
        throw RuntimeException("Cancel")
    }
}