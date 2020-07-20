package it.zoo.spring.idea.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.util.PsiTreeUtil
import it.zoo.spring.idea.plugin.storage.ProjectStorage
import org.jetbrains.kotlin.psi.KtClass

class AddDtoAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val ktClass = extractPsiClass(e)
        if (ktClass == null) {
            JBPopupFactory.getInstance()
                .createMessage("Dto isn't selected, plugin works only for kotlin,please  move your mouse into your dto class")
                .showInFocusCenter()
            return
        }
        ProjectStorage.modelDto = ktClass
    }

    private fun extractPsiClass(anActionEvent: AnActionEvent): KtClass? {
        val psiFile = requireNotNull(anActionEvent.getData(LangDataKeys.PSI_FILE)) { "psiFile must not be null" }
        return PsiTreeUtil.findChildOfType(psiFile, KtClass::class.java)
    }
}