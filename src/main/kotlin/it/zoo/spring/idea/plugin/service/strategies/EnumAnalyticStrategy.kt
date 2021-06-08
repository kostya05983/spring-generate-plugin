package it.zoo.spring.idea.plugin.service.strategies

import com.intellij.openapi.project.Project
import it.zoo.spring.idea.plugin.model.*
import it.zoo.spring.idea.plugin.service.AnalyticStrategy
import org.jetbrains.kotlin.psi.KtClass
import java.util.*

class EnumAnalyticStrategy(
    override val project: Project
) : AnalyticStrategy {
    override fun analytic(
        model: KtClass,
        dto: KtClass,
        stack: Stack<DtoModelPair>
    ): Converter {
        val modelShortName = model.fqName!!.shortName().identifier
        val dtoShortName = dto.fqName!!.shortName().identifier
        val convertedElements = model.declarations.map { declaration ->
            when (val dtoDeclaration = dto.declarations.firstOrNull { it.name == declaration.name }) {
                null -> {
                    SimpleConvertedElement(
                        from = "$modelShortName.${declaration.name}",
                        to = null
                    )
                }
                else -> {
                    SimpleConvertedElement(
                        from = "$modelShortName.${declaration.name}",
                        to = "$dtoShortName.${dtoDeclaration.name}"
                    )
                }
            }
        }
        return EnumClassConverter(
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
}