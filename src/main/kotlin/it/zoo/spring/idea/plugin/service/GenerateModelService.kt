package it.zoo.spring.idea.plugin.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.intellij.psi.search.GlobalSearchScope
import it.zoo.spring.idea.plugin.model.ConvertedElement
import it.zoo.spring.idea.plugin.model.Converter
import it.zoo.spring.idea.plugin.storage.ProjectStorage
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.util.*

class GenerateModelService(
    private val virtualFile: VirtualFile,
    private val project: Project
) {
    private val codeGenerator = CodeGenerator()

    fun generate() {
        val converters = analytic()
        val files = converters.map {
            val str = codeGenerator.formString(it)
            KtPsiFactory(project).createFile("${it.name}.kt", str)
        }

        val application = ApplicationManager.getApplication()

        val directory = PsiDirectoryFactory.getInstance(project).createDirectory(virtualFile)
        files.forEach {
            CodeStyleManager.getInstance(project).reformat(it)

        }
        application.runWriteAction {
            files.forEach {
                directory.add(it)
            }
        }
    }

    private fun analytic(): List<Converter> {
        val model = ProjectStorage.model!! // TODO
        val modelDto = ProjectStorage.modelDto!! // TODO
        val stack = Stack<DtoModelPair>()
        stack.push(
            DtoModelPair(modelDto, model)
        )
        val result = mutableListOf<Converter>()
        while (stack.isNotEmpty()) {
            val pair = stack.pop()
            when {
                modelDto.isData() -> {
                    val pairs = analyticDataClass(pair)
                    val tasks = pairs.mapNotNull { it.first }
                    tasks.forEach { stack.push(it) }

                    val convertedElements = pairs.map { it.second }

                    val converter = Converter(
                        name = "${pair.dto.name}Converter",
                        from = pair.model.name!!,
                        to = pair.dto.name!!,
                        imports = listOfNotNull(
                            pair.dto.fqName?.asString(),
                            pair.model.fqName?.asString()
                        ),
                        elements = convertedElements
                    )
                    result.add(converter)
                }
                modelDto.isEnum() -> {
                    val modelShortName = model.fqName!!.shortName().identifier
                    val dtoShortName = modelDto.fqName!!.shortName().identifier
                    val convertedElements = model.declarations.map { declaration ->
                        val dtoDeclaration = modelDto.declarations.firstOrNull { it.name == declaration.name }
                        when {
                            dtoDeclaration == null -> {
                                ConvertedElement(
                                    from = "$modelShortName.${declaration.name}",
                                    to = "TODO()",
                                    type = ConvertedElement.Type.SIMPLE
                                )
                            }
                            else -> {
                                ConvertedElement(
                                    from = "$modelShortName.${declaration.name}",
                                    to = "$dtoShortName.${dtoDeclaration.name}",
                                    type = ConvertedElement.Type.SIMPLE
                                )
                            }
                        }
                    }
                    val converter = Converter(
                        name = "${pair.dto.name}Converter",
                        from = pair.model.name!!,
                        to = pair.dto.name!!,
                        imports = listOfNotNull(
                            pair.dto.fqName?.asString(),
                            pair.model.fqName?.asString()
                        ),
                        elements = convertedElements,
                        typeClass = Converter.TypeClass.ENUM
                    )
                    result.add(converter)
                }
                modelDto.isSealed() -> {
                    TODO()
                }
                else -> TODO()
            }
        }
        return result
    }

    data class DtoModelPair(
        val dto: KtClass,
        val model: KtClass
    )

    private fun analyticDataClass(modelDtoPair: DtoModelPair): List<Pair<DtoModelPair?, ConvertedElement>> {
        val parameters = modelDtoPair.dto.primaryConstructorParameters
        return parameters.map { valueParameter ->
            val modelValueParameter =
                modelDtoPair.model.primaryConstructorParameters.firstOrNull { it.name == valueParameter.name }
            when {
                modelValueParameter == null -> {
                    Pair(null, ConvertedElement(valueParameter.name!!, "TODO()", null, ConvertedElement.Type.SIMPLE))
                }
                modelValueParameter.type() == valueParameter.type() -> {
                    Pair(
                        null, ConvertedElement(
                            valueParameter.name!!,
                            modelValueParameter.name!!,
                            null,
                            ConvertedElement.Type.SIMPLE
                        )
                    )
                }
                modelValueParameter.type() != valueParameter.type() -> {
                    val dtoShortName = valueParameter.type()?.fqName?.shortName()?.identifier!!
                    val modelShortName = modelValueParameter.type()?.fqName?.shortName()?.identifier!!
                    val dtoKClass = KotlinClassShortNameIndex.getInstance()
                        .get(dtoShortName, project, GlobalSearchScope.allScope(project)).first() as KtClass
                    val modelKClass = KotlinClassShortNameIndex.getInstance()
                        .get(modelShortName, project, GlobalSearchScope.allScope(project)).first() as KtClass
                    val task = DtoModelPair(dtoKClass, modelKClass)

                    if (valueParameter.type()!!.isMarkedNullable) {
                        Pair(
                            task, ConvertedElement(
                                valueParameter.name!!,
                                modelValueParameter.name!!,
                                dtoShortName,
                                ConvertedElement.Type.NULLABLE_CONVERT
                            )
                        )
                    } else {
                        Pair(
                            task, ConvertedElement(
                                valueParameter.name!!,
                                modelValueParameter.name!!,
                                dtoShortName,
                                ConvertedElement.Type.CONVERT
                            )
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
}