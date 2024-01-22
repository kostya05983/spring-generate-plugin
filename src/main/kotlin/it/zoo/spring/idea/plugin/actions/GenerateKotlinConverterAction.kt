package it.zoo.spring.idea.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import it.zoo.spring.idea.plugin.service.GenerateModelService
import it.zoo.spring.idea.plugin.service.GeneratorStyle

class GenerateKotlinConverterAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val virtualFile = e.getData(LangDataKeys.VIRTUAL_FILE) ?: return
        val project = e.project ?: kotlin.run {
            println("Can't find project, skip")
            return
        }
        val service = GenerateModelService(virtualFile, project, GeneratorStyle.KOTLIN)
        service.generate()
    }
}