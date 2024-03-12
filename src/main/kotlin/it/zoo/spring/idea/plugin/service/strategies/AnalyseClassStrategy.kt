package it.zoo.spring.idea.plugin.service.strategies

import com.intellij.openapi.project.Project
import it.zoo.spring.idea.plugin.model.*
import it.zoo.spring.idea.plugin.service.AnalyseStrategy
import it.zoo.spring.idea.plugin.service.GeneratorStyle
import it.zoo.spring.idea.plugin.utils.KotlinIndexUtils
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.idea.base.utils.fqname.fqName
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.types.KotlinType
import java.util.*

class AnalyseClassStrategy(
    override val project: Project,
    private val generatorStyle: GeneratorStyle
) : AnalyseStrategy {

    override fun analyse(
        model: KtClass,
        dto: KtClass,
        stack: Stack<DtoModelPair>
    ): Converter {
        val pairs = analyseDataClass(model, dto)
        val tasks = pairs.mapNotNull { it.first }
        tasks.forEach { stack.push(it) }

        val convertedElements = pairs.map { it.second }

        val dtoName = requireNotNull(dto.name) { "Dto name must not be null" }
        return ClassConverter(
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

    private fun analyseDataClass(
        model: KtClass,
        dto: KtClass
    ): List<Pair<DtoModelPair?, ConvertedElement>> {
        val parameters = dto.primaryConstructorParameters

        return parameters.map { dtoParameter ->
            val modelValueParameter = model.primaryConstructorParameters.firstOrNull { it.name == dtoParameter.name }
            val modelParameterType = modelValueParameter?.type()
            val dtoParameterType = requireNotNull(dtoParameter.type()) { "Parameter type must not be null" }

            when {
                modelParameterType == null -> {
                    Pair(
                        null,
                        SimpleConvertedElement(
                            dtoParameter.name!!,
                            null
                        )
                    )
                }

                modelParameterType.equalsConverted(dtoParameterType) -> {
                    Pair(
                        null,
                        SimpleConvertedElement(
                            dtoParameter.name!!,
                            modelValueParameter.name!!
                        )
                    )
                }

                modelParameterType.equalsConverted(dtoParameterType).not() -> {
                    val dtoShortName = dtoParameterType.shortNameIdentifier()
                    val modelShortName = modelParameterType.shortNameIdentifier()

                    if (dtoShortName == "List" && modelShortName == "List") {
                        val dtoListType = dtoParameterType.arguments[0].type

                        val (task, convertedElement) = convertUnmatch(
                            dtoParameter.name!!,
                            modelValueParameter.name!!,
                            dtoListType,
                            modelParameterType.arguments[0].type,
                        )
                        Pair(
                            task,
                            ListConvertedElement(
                                isNullableConvert = modelParameterType.isMarkedNullable,
                                from = dtoParameter.name!!,
                                to = modelValueParameter.name!!,
                                element = convertedElement
                            )
                        )
                    } else {
                        convertUnmatch(
                            dtoParameter.name!!,
                            modelValueParameter.name!!,
                            dtoParameterType,
                            modelParameterType
                        )
                    }
                }

                else -> Pair(
                    null, SimpleConvertedElement(
                        dtoParameter.name!!,
                        null
                    )
                )
            }
        }
    }

    private fun convertUnmatch(
        dtoParameterName: String,
        modelParameterName: String,
        dtoParameter: KotlinType,
        modelParameter: KotlinType,
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
                dtoShortName
            )
        )
    }

    private fun KotlinType.equalsConverted(other: KotlinType): Boolean {
        return this.fqName == other.fqName && this.arguments == other.arguments
    }

    private fun KotlinType.shortNameIdentifier(): String {
        return requireNotNull(fqName?.shortName()?.identifier) { "ShortName identifier must exists" }
    }

    private fun KtDeclaration.type() =
        (resolveToDescriptorIfAny() as? CallableDescriptor)?.returnType
}