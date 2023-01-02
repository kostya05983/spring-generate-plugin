package it.zoo.spring.idea.plugin.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.psi.KtClass

object KotlinIndexUtils {
    fun getKClass(fqName: String, project: Project): KtClass? {
        return KotlinFullClassNameIndex.get(fqName, project, GlobalSearchScope.allScope(project))
            .firstOrNull {
                it.fqName?.asString() == fqName
            } as? KtClass
    }
}