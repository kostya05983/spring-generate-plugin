package it.zoo.spring.idea.plugin.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.file.PsiDirectoryFactory
import it.zoo.spring.idea.plugin.model.*
import it.zoo.spring.idea.plugin.service.strategies.AnalyticClassStrategy
import it.zoo.spring.idea.plugin.service.strategies.EnumAnalyticStrategy
import it.zoo.spring.idea.plugin.service.strategies.SealedAnalyticStrategy
import it.zoo.spring.idea.plugin.storage.ProjectStorage
import org.jetbrains.kotlin.idea.core.getPackage
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.util.*

class GenerateModelService(
    private val virtualFile: VirtualFile,
    private val project: Project
) {
    private val codeGenerator = CodeGenerator()
    private val sealedAnalyticStrategy = SealedAnalyticStrategy(project)
    private val classAnalyticStrategy = AnalyticClassStrategy(project)
    private val enumAnalyticStrategy = EnumAnalyticStrategy(project)

    fun generate() {
        val converters = analytic()
        val pack = PsiManager.getInstance(project).findDirectory(virtualFile)?.getPackage()?.qualifiedName ?: ""
        val files = converters.map {
            val str = codeGenerator.formString(it, pack)
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
            val model = pair.model
            val dto = pair.dto
            when {
                dto.isData() -> {
                    val converter = classAnalyticStrategy.analytic(model, dto, stack)
                    result.add(converter)
                }
                dto.isEnum() -> {
                    val converter = enumAnalyticStrategy.analytic(model, dto, stack)
                    result.add(converter)
                }
                dto.isSealed() -> {
                    val converter = sealedAnalyticStrategy.analytic(model, dto, stack)
                    result.add(converter)
                }
                else -> classAnalyticStrategy.analytic(model, dto, stack)
            }
        }
        return result
    }
}