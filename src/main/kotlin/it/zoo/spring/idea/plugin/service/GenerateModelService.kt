package it.zoo.spring.idea.plugin.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.file.PsiDirectoryFactory
import it.zoo.spring.idea.plugin.storage.ProjectStorage
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.lang.StringBuilder

class GenerateModelService(
    private val virtualFile: VirtualFile,
    private val project: Project
) {
    data class ConvertedElement(
        val from: String,
        val to: String,
        val toType: String? = null,
        val type: Type
    ) {
        enum class Type {
            SIMPLE,
            NULLABLE_CONVERT,
            CONVERT
        }
    }

    data class Converter(
        val name: String,
        val from: String,
        val to: String,
        val elements: List<ConvertedElement>
    )

    fun generate() {
        val converter = analytic()
        val str = formString(converter)
        val ktFile = KtPsiFactory(project).createFile("${converter.name}.kt", str)

        val application = ApplicationManager.getApplication()


        val directory = PsiDirectoryFactory.getInstance(project).createDirectory(virtualFile)

        CodeStyleManager.getInstance(project).reformat(ktFile)
        application.runWriteAction {
            directory.add(ktFile)
        }
    }

    private fun analytic(): Converter {
        val model = ProjectStorage.model!! // TODO
        val modelDto = ProjectStorage.modelDto!! // TODO

        if (modelDto.isData()) {
            val primaryConstructor = modelDto.primaryConstructor!! // TODO data class without primary constructor
            val convertedElements = primaryConstructor.valueParameters.map { valueParameter ->
                val modelValueParameter = model.primaryConstructor!!.valueParameterList!!.parameters.firstOrNull {
                    it.name == valueParameter.name
                }
                when {
                    modelValueParameter == null -> {
                        ConvertedElement(valueParameter.name!!, "TODO()", null, ConvertedElement.Type.SIMPLE)
                    }
                    modelValueParameter.valOrVarKeyword?.text == valueParameter.valOrVarKeyword?.text -> {
                        ConvertedElement(
                            valueParameter.name!!,
                            modelValueParameter.name!!,
                            null,
                            ConvertedElement.Type.SIMPLE
                        )
                    }
                    modelValueParameter.valOrVarKeyword?.text != valueParameter.valOrVarKeyword?.text -> {
                        //TODO check is optional
                        ConvertedElement(
                            valueParameter.name!!,
                            modelValueParameter.name!!,
                            "SomeType",
                            ConvertedElement.Type.CONVERT
                        )
                    }
                    else -> ConvertedElement(valueParameter.name!!, "TODO()", null, ConvertedElement.Type.SIMPLE)
                }
            }
            return Converter(
                name = "${modelDto.name}Converter",
                from = model.name!!,
                to = modelDto.name!!,
                elements = convertedElements
            )
        } else {
            TODO()
        }
    }

    fun formString(converter: Converter): String {
        val sb = StringBuilder()
        sb.append("import org.springframework.core.convert.converter.Converter\n")
        sb.append("object ${converter.name}: Converter<${converter.from}, ${converter.to}>{\n")
        sb.append("override fun convert(source: ${converter.from}): ${converter.to} {\n")
        sb.append("return ${converter.to}(\n")

        for (it in converter.elements) {
            when (it.type) {
                ConvertedElement.Type.SIMPLE -> sb.append("${it.from} = ${it.to}")
                ConvertedElement.Type.NULLABLE_CONVERT -> sb.append("${it.from}=${it.to}?.let{${it.to}Converter.convert(it)}")
                ConvertedElement.Type.CONVERT -> sb.append("${it.from}=${it.to}Converter.convert")
            }
            if (it == converter.elements.last()) {
                sb.append("\n")
            } else {
                sb.append(",\n")
            }
        }
        sb.append(")\n")
        sb.append("}\n")
        sb.append("}\n")
        return sb.toString()
    }
}