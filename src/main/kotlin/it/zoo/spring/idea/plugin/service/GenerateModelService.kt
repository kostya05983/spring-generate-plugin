package it.zoo.spring.idea.plugin.service

import it.zoo.spring.idea.plugin.storage.ProjectStorage
import org.jetbrains.kotlin.psi.KtClass

class GenerateModelService {

    data class GeneratedFile(
        val name: String,
        val text: String
    )

    fun generate() {
        val model = ProjectStorage.model!! // TODO
        val modelDto = ProjectStorage.modelDto!! // TODO

        if (model.isData()) {
            val primaryConstructor = model.primaryConstructor!! // TODO data class without primary constructor
            primaryConstructor.valueParameters

        }
    }
}