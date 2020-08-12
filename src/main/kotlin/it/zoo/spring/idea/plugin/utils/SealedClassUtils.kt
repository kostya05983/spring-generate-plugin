package it.zoo.spring.idea.plugin.utils

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.psiUtil.getSuperNames

object SealedClassUtils {
    fun findInheritorsOfSealedClass(model: KtClass, project: Project): List<KtClass> {
        return findInFileSealed(model, project) + findInClassSealed(model)
    }

    private fun findInFileSealed(model: KtClass, project: Project): List<KtClass> {
        val filtered = model.containingKtFile.classes.filter { it.superClass?.name == model.name }
        return filtered.mapNotNull {
            it.name?.let { KotlinIndexUtils.getKClass(it, project) }
        }
    }

    private fun findInClassSealed(model: KtClass): List<KtClass> {
        return model.children.find { it is KtClassBody }?.children?.filterIsInstance<KtClass>()?.filter {
            it.getSuperNames().contains(model.name!!)
        } ?: listOf()
    }
}