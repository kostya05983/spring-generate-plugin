package it.zoo.spring.idea.plugin.storage

import org.jetbrains.kotlin.psi.KtClass

object ProjectStorage {
    var model: KtClass? = null
    var dto: KtClass? = null
}