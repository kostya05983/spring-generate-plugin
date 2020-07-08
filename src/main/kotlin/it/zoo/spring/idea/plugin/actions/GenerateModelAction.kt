package it.zoo.spring.idea.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import org.jetbrains.kotlin.psi.KtClass
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import it.zoo.spring.idea.plugin.dialogs.GenerateModelDialog
import it.zoo.spring.idea.plugin.service.GenerateModelService
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.*

/**
 * @author Konstantin Volivach
 */
class GenerateModelAction : AnAction() {

    private companion object {
        const val KOTLIN_EXTENSION = "kotlin"
    }

    private val service = GenerateModelService()

    override fun actionPerformed(e: AnActionEvent) {
        val ktClass = requireNotNull(extractPsiClass(e)) { "Psi class can't be null" }
        val generateModelDialog = GenerateModelDialog(ktClass)

        generateModelDialog.showDialog()
    }

    private fun extractPsiClass(anActionEvent: AnActionEvent): KtClass? {
        val psiFile = requireNotNull(anActionEvent.getData(LangDataKeys.PSI_FILE)) { "psiFile must not be null" }
        return PsiTreeUtil.findChildOfType(psiFile, KtClass::class.java)
    }

    private fun isKotlinFile(psiFile: PsiFile): Boolean {
        return psiFile.fileType.defaultExtension == KOTLIN_EXTENSION
    }
}