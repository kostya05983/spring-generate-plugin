package it.zoo.spring.idea.plugin.model

import org.jetbrains.kotlin.psi.KtClass

data class DtoModelPair(
    val dto: KtClass,
    val model: KtClass
)