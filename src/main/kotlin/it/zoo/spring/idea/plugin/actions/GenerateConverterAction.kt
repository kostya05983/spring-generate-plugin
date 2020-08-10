package it.zoo.spring.idea.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import it.zoo.spring.idea.plugin.service.GenerateModelService

class GenerateConverterAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val virtualFile = e.getData(LangDataKeys.VIRTUAL_FILE) ?: return
        val service = GenerateModelService(virtualFile, e.project!!)
        service.generate()
    }
}