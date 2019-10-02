package it.zoo.spring.idea.plugin.service

import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiUtil

class GenerateDAOService {

    fun generate(psiClass: PsiClass) {
        println("Deepness ${countDeep(psiClass)}")
    }

    fun countDeep(psiClass: PsiClass): Int {
        val fields = psiClass.allFields

        var count = 0
        for (field in fields) {
            val containingClass = field.containingClass
            if (containingClass != null && PsiUtil.isFromDefaultPackage(field)) {
                count += countDeep(containingClass)
            }
        }
        return count
    }
}