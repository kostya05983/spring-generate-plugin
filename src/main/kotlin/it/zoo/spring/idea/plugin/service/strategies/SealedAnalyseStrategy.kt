package it.zoo.spring.idea.plugin.service.strategies

import com.intellij.openapi.project.Project
import it.zoo.spring.idea.plugin.model.*
import it.zoo.spring.idea.plugin.service.AnalyseStrategy
import it.zoo.spring.idea.plugin.service.GeneratorStyle
import it.zoo.spring.idea.plugin.utils.SealedClassUtils
import org.jetbrains.kotlin.psi.KtClass
import java.util.*

class SealedAnalyseStrategy(
    override val project: Project,
    private val generatorStyle: GeneratorStyle
) : AnalyseStrategy {
    override fun analyse(
        model: KtClass,
        dto: KtClass,
        stack: Stack<DtoModelPair>
    ): Converter {
        val modelChildren = SealedClassUtils.findInheritorsOfSealedClass(model, project)
        val dtoChildren = SealedClassUtils.findInheritorsOfSealedClass(dto, project)
        val childrenImports = modelChildren.mapNotNull {
            it.fqName?.asString()
        }
        val convertedElements = modelChildren.map { modelClass ->
            val dtoClass = dtoChildren.find { stupidMaxMatch(modelClass.name!!, it.name!!) }
            dtoClass?.let {
                val pair = DtoModelPair(
                    model = modelClass,
                    dto = dtoClass
                )
                stack.push(pair)
            }

            SimpleConvertedElement(
                from = modelClass.name!!,
                to = dtoClass?.name
            )
        }

        val dtoName = requireNotNull(dto.name) { "Dto name must not be null" }

        return SealedClassConverter(
            name = generatorStyle.getFileName(dtoName),
            from = model.name!!,
            to = dto.name!!,
            imports = listOfNotNull(
                dto.fqName?.asString(),
                model.fqName?.asString()
            ) + childrenImports,
            elements = convertedElements
        )
    }

    private fun stupidMaxMatch(name: String, other: String): Boolean {
        return if (name.length < other.length) {
            other.contains(name)
        } else {
            name.contains(other)
        }
    }
}