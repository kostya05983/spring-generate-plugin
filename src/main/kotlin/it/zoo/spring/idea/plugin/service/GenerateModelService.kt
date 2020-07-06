package it.zoo.spring.idea.plugin.service

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.allChildren

class GenerateModelService {

    fun generate(psiClass: KtClass) {
        println("Deepness ${countDeep(psiClass)}")
    }

    fun countDeep(psiClass: PsiElement): Int {
        val childs = psiClass.allChildren

        var count = 0
        for (child in childs) {
            if (child.children.isNotEmpty()) {
                count += countDeep(child)
            }
        }
        return count
    }
}