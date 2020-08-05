package it.zoo.spring.idea.plugin.service.strategies

import com.intellij.openapi.project.Project
import it.zoo.spring.idea.plugin.model.ConvertedElement
import it.zoo.spring.idea.plugin.model.Converter
import it.zoo.spring.idea.plugin.model.DtoModelPair
import it.zoo.spring.idea.plugin.model.SealedClassConverter
import it.zoo.spring.idea.plugin.service.AnalyticStrategy
import it.zoo.spring.idea.plugin.utils.SealedClassUtils
import org.jetbrains.kotlin.psi.KtClass
import java.util.*

class SealedAnalyticStrategy(
    override val project: Project
) : AnalyticStrategy {
    override fun analytic(
        model: KtClass,
        dto: KtClass,
        stack: Stack<DtoModelPair>
    ): Converter {
        val modelChildren = SealedClassUtils.findInheritorsOfSealedClass(model, project)
        val dtoChildren = SealedClassUtils.findInheritorsOfSealedClass(dto, project)
        val convertedElements = modelChildren.map { modelClass ->
            val dtoClass = dtoChildren.find { stupidMaxMatch(modelClass.name!!, it.name!!) }
            dtoClass?.let {
                val pair = DtoModelPair(
                    model = modelClass,
                    dto = dtoClass
                )
                stack.push(pair)
            }

            ConvertedElement(
                from = modelClass.name!!,
                to = dtoClass?.name ?: "TODO()",
                type = ConvertedElement.Type.SIMPLE
            )
        }

        return SealedClassConverter(
            name = "${dto.name}Converter",
            from = model.name!!,
            to = dto.name!!,
            imports = listOfNotNull(
                dto.fqName?.asString(),
                model.fqName?.asString()
            ),
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