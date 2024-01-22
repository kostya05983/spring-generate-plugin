package it.zoo.spring.idea.plugin.service

import com.intellij.openapi.project.Project
import it.zoo.spring.idea.plugin.model.Converter
import it.zoo.spring.idea.plugin.model.DtoModelPair
import org.jetbrains.kotlin.psi.KtClass
import java.util.*

interface AnalyseStrategy {
    val project: Project

    fun analyse(
        model: KtClass,
        dto: KtClass,
        stack: Stack<DtoModelPair>
    ): Converter
}