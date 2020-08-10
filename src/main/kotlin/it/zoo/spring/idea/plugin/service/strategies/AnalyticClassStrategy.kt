package it.zoo.spring.idea.plugin.service.strategies

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import it.zoo.spring.idea.plugin.model.ConvertedElement
import it.zoo.spring.idea.plugin.model.Converter
import it.zoo.spring.idea.plugin.model.ClassConverter
import it.zoo.spring.idea.plugin.model.DtoModelPair
import it.zoo.spring.idea.plugin.service.AnalyticStrategy
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
                    Pair(null, ConvertedElement(valueParameter.name!!, "TODO()", null, ConvertedElement.Type.SIMPLE))
                }
                modelType.equalsConverted(valueType) -> {
                    Pair(
                        null, ConvertedElement(
                            valueParameter.name!!,
                            modelValueParameter.name!!,
                            null,
                            ConvertedElement.Type.SIMPLE
                        )
                    )
                }
                modelType.equalsConverted(valueType).not() -> {
                    val dtoShortName = valueType.fqName?.shortName()?.identifier!!
                    val modelShortName = modelType.fqName?.shortName()?.identifier!!
                    if (dtoShortName == "List" && modelShortName == "List") {
                        convertUnmatch(
                            valueParameter.name!!,
                            modelValueParameter.name!!,
                            valueType.arguments[0].type,
                            modelType.arguments[0].type,
                            ConvertedElement.Type.LIST_CONVERT,
                            ConvertedElement.Type.NULLABLE_LIST_CONVERT
                        )
                    } else {
                        convertUnmatch(
                            valueParameter.name!!,
                            modelValueParameter.name!!,
                            valueType,
                            modelType,
                            ConvertedElement.Type.CONVERT,
                            ConvertedElement.Type.NULLABLE_CONVERT
                        )
                    }
                }
                else -> Pair(
                    null, ConvertedElement(
                        valueParameter.name!!,
                        "TODO()",
                        null,
                        ConvertedElement.Type.SIMPLE
                    )
                )
            }
        }
    }

    private fun convertUnmatch(
        dtoParameterName: String,
        modelParameterName: String,
        dtoParamter: KotlinType,
        modelParameter: KotlinType,
        type: ConvertedElement.Type,
        nullableType: ConvertedElement.Type
    ): Pair<DtoModelPair?, ConvertedElement> {
        val dtoShortName = dtoParamter.fqName?.shortName()?.identifier!!
        val modelShortName = modelParameter.fqName?.shortName()?.identifier!!

        val dtoKClass = KotlinClassShortNameIndex.getInstance()
            .get(dtoShortName, project, GlobalSearchScope.allScope(project)).firstOrNull() as? KtClass
        val modelKClass = KotlinClassShortNameIndex.getInstance()
            .get(modelShortName, project, GlobalSearchScope.allScope(project)).firstOrNull() as? KtClass
        val task = if (dtoKClass != null && modelKClass != null) DtoModelPair(
            dtoKClass,
            modelKClass
        ) else null

        return if (dtoParamter.isMarkedNullable) {
            Pair(
                task, ConvertedElement(
                    dtoParameterName,
                    modelParameterName,
                    dtoShortName,
                    nullableType
                )
            )
        } else {
            Pair(
                task, ConvertedElement(
                    dtoParameterName,
                    modelParameterName,
                    dtoShortName,
                    type
                )
            )
        }
    }
}