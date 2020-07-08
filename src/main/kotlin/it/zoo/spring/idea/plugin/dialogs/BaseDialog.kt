package it.zoo.spring.idea.plugin.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper

abstract class BaseDialog(
    project: Project
) : DialogWrapper(project, true) {

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