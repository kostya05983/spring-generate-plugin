package it.zoo.spring.idea.plugin.service.strategies

import com.intellij.openapi.project.Project
import it.zoo.spring.idea.plugin.model.Converter
import it.zoo.spring.idea.plugin.model.DtoModelPair
import it.zoo.spring.idea.plugin.model.EnumClassConverter
import it.zoo.spring.idea.plugin.model.SimpleConvertedElement
import it.zoo.spring.idea.plugin.service.AnalyseStrategy
import it.zoo.spring.idea.plugin.service.GeneratorStyle
import org.jetbrains.kotlin.psi.KtClass
import java.util.*

class EnumAnalyseStrategy(
    override val project: Project,
    private val generatorStyle: GeneratorStyle
) : AnalyseStrategy {
    override fun analyse(
        model: KtClass,
        dto: KtClass,
        stack: Stack<DtoModelPair>
    ): Converter {
        val modelShortName = model.fqName!!.shortName().identifier
        val dtoShortName = dto.fqName!!.shortName().identifier

        val ignoreNames = hashSetOf("toString")
        model.getProperties().forEach {
            it.name?.let { ignoreNames.add(it) }
        }

        val convertedElements = model.declarations.mapNotNull { declaration ->
            if (ignoreNames.contains(declaration.name)) return@mapNotNull null

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

        val dtoName = requireNotNull(dto.name) { "Dto name must not be null" }

        return EnumClassConverter(
            name = generatorStyle.getFileName(dtoName),
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