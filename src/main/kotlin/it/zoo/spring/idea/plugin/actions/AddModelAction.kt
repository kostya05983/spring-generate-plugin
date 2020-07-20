package it.zoo.spring.idea.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import org.jetbrains.kotlin.psi.KtClass
import com.intellij.psi.util.PsiTreeUtil
import it.zoo.spring.idea.plugin.dialogs.GenerateModelDialog
import it.zoo.spring.idea.plugin.service.GenerateModelService

/**
 * @author Konstantin Volivach
 */
class AddModelAction : AnAction() {
    private val service = GenerateModelService()

    override fun actionPerformed(e: AnActionEvent) {
        val ktClass = requireNotNull(extractPsiClass(e)) { "Psi class can't be null" } // TODO show popup
        val generateModelDialog = GenerateModelDialog(ktClass)

        generateModelDialog.showDialog()
    }

    private fun extractPsiClass(anActionEvent: AnActionEvent): KtClass? {
        val psiFile = requireNotNull(anActionEvent.getData(LangDataKeys.PSI_FILE)) { "psiFile must not be null" }
        return PsiTreeUtil.findChildOfType(psiFile, KtClass::class.java)
    }
}