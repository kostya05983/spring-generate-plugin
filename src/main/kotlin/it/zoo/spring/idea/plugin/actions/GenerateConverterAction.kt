package it.zoo.spring.idea.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.file.PsiDirectoryFactory
import org.jetbrains.kotlin.idea.KotlinLanguage

class GenerateConverterAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val virtualFile = e.getData(LangDataKeys.VIRTUAL_FILE) ?: return // TODO

        val directory = PsiDirectoryFactory.getInstance(e.project).createDirectory(virtualFile)

        val ktFile =
            PsiFileFactory.getInstance(e.project)
                .createFileFromText("Test.kt", KotlinLanguage.INSTANCE, "data class HUI()")
        val application = ApplicationManager.getApplication()
        application.runWriteAction {
            directory.add(ktFile)
        }
    }
}