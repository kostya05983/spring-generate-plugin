package it.zoo.spring.idea.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import it.zoo.spring.idea.plugin.service.GenerateDAOService

/**
 * @author Konstantin Volivach
 */
class GenerateDAOAction : AnAction() {

    private companion object {
        const val KOTLIN_EXTENSION = "kotlin"
    }

    private val service = GenerateDAOService()

    override fun actionPerformed(e: AnActionEvent) {
        val psiClass = requireNotNull(extractPsiClass(e)) { "Psi class can't be null" }
        service.generate(psiClass)
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