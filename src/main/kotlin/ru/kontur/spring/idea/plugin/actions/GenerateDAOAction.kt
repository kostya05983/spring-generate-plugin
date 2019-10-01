package ru.kontur.spring.idea.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

class GenerateDAOAction : AnAction() {

    private companion object {
        const val KOTLIN_EXTENSION = "kotlin"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val psiClass = extractPsiClass(e)
    }

    private fun extractPsiClass(anActionEvent: AnActionEvent): PsiClass? {
        val psiFile = requireNotNull(anActionEvent.getData(LangDataKeys.PSI_FILE)) { "psiFile must not be null" }
        val editor = requireNotNull(anActionEvent.getData(PlatformDataKeys.EDITOR)) { "editor must not be null" }
        val elementAt = psiFile.findElementAt(editor.caretModel.offset)
        return PsiTreeUtil.getParentOfType(elementAt, PsiClass::class.java)
    }

    private fun isKotlinFile(psiFile: PsiFile): Boolean {
        return psiFile.fileType.defaultExtension == KOTLIN_EXTENSION
    }
}