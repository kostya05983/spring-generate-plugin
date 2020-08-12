package it.zoo.spring.idea.plugin.service.strategies

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import it.zoo.spring.idea.plugin.model.*
import it.zoo.spring.idea.plugin.service.AnalyticStrategy
import it.zoo.spring.idea.plugin.utils.KotlinIndexUtils
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.types.KotlinType
import java.util.*

class AnalyticClassStrategy(
    override val project: Project
) : AnalyticStrategy {
    override fun analytic(
        model: KtClass,
        dto: KtClass,
        stack: Stack<DtoModelPair>
    ): Converter {
        val pairs = analyticDataClass(model, dto)
        val tasks = pairs.mapNotNull { it.first }
        tasks.forEach { stack.push(it) }

        val convertedElements = pairs.map { it.second }

        return ClassConverter(
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

    private fun KotlinType.equalsConverted(other: KotlinType): Boolean {
        return this.fqName == other.fqName && this.arguments == other.arguments
    }

    private fun analyticDataClass(
        model: KtClass,
        dto: KtClass
    ): List<Pair<DtoModelPair?, ConvertedElement>> {
        val parameters = dto.primaryConstructorParameters
        return parameters.map { valueParameter ->
            val modelValueParameter =
                model.primaryConstructorParameters.firstOrNull { it.name == valueParameter.name }
            val modelType = modelValueParameter?.type()
            val valueType = valueParameter.type()!!
            when {
                modelType == null -> {
                    Pair(
                        null,
                        SimpleConvertedElement(
                            valueParameter.name!!,
                            "TODO()"
                        )
                    )
                }
                modelType.equalsConverted(valueType) -> {
                    Pair(
                        null,
                        SimpleConvertedElement(
                            valueParameter.name!!,
                            modelValueParameter.name!!
                        )
                    )
                }
                modelType.equalsConverted(valueType).not() -> {
                    val dtoShortName = valueType.fqName?.shortName()?.identifier!!
                    val modelShortName = modelType.fqName?.shortName()?.identifier!!
                    if (dtoShortName == "List" && modelShortName == "List") {
                        val (task, convertedElement) = convertUnmatch(
                            valueParameter.name!!,
                            modelValueParameter.name!!,
                            valueType.arguments[0].type,
                            modelType.arguments[0].type
                        )
                        Pair(
                            task,
                            ListConvertedElement(
                                isNullableConvert = modelType.isMarkedNullable,
                                from = valueParameter.name!!,
                                to = modelValueParameter.name!!,
                                element = convertedElement
                            )
                        )
                    } else {
                        convertUnmatch(
                            valueParameter.name!!,
                            modelValueParameter.name!!,
                            valueType,
                            modelType
                        )
                    }
                }
                else -> Pair(
                    null, SimpleConvertedElement(
                        valueParameter.name!!,
                        "TODO()"
                    )
                )
            }
        }
    }

    private fun convertUnmatch(
        dtoParameterName: String,
        modelParameterName: String,
        dtoParameter: KotlinType,
        modelParameter: KotlinType
    ): Pair<DtoModelPair?, ConvertedElement> {
        val dtoShortName = dtoParameter.fqName?.shortName()?.identifier

        val dtoKClass = dtoParameter.fqName?.let {
            KotlinIndexUtils.getKClass(it.asString(), project)
        }
        val modelKClass = modelParameter.fqName?.let {
            KotlinIndexUtils.getKClass(it.asString(), project)
        }
        val task = if (dtoKClass != null && modelKClass != null) DtoModelPair(
            dtoKClass,
            modelKClass
        ) else null

        return Pair(
            task, ConvertConvertedElement(
                dtoParameter.isMarkedNullable,
                dtoParameterName,
                modelParameterName,
                dtoShortName ?: "TODO()"
            )
        )
    }
}