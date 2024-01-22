package it.zoo.spring.idea.plugin.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.file.PsiDirectoryFactory
import it.zoo.spring.idea.plugin.model.Converter
import it.zoo.spring.idea.plugin.service.generators.CompositeConverterGenerator
import it.zoo.spring.idea.plugin.storage.ProjectStorage
import org.jetbrains.kotlin.idea.core.getPackage
import org.jetbrains.kotlin.psi.KtPsiFactory

class GenerateModelService(
    private val virtualFile: VirtualFile,
    private val project: Project,
    private val generatorStyle: GeneratorStyle
) {
    private val codeGenerator = CompositeConverterGenerator(generatorStyle)
    private val analyseService = AnalyseConverterService(project, generatorStyle)

    fun generate() {
        val model = ProjectStorage.model ?: let {
            JBPopupFactory.getInstance().createMessage("You need to fill model from").showInFocusCenter()
            return
        }
        val dto = ProjectStorage.dto ?: let {
            JBPopupFactory.getInstance().createMessage("You need to fill model to").showInFocusCenter()
            return
        }
        val convertersTo = analyseService.analyse(model, dto)
        val convertersFrom = analyseService.analyse(dto, model)

        val packageName = PsiManager.getInstance(project).findDirectory(virtualFile)?.getPackage()?.qualifiedName ?: ""

        val files = convertersTo.mapIndexed { index: Int, converter: Converter ->
            val str = codeGenerator.getString(converter, packageName,)

            if (generatorStyle == GeneratorStyle.KOTLIN) {
//                val reverseConverter = codeGenerator.getString(convertersFrom[index], packageName)
                KtPsiFactory(project).createFile("${converter.name}.kt", str)
            } else {
                KtPsiFactory(project).createFile("${converter.name}.kt", str)
            }
        }

        val application = ApplicationManager.getApplication()

        val directory = PsiDirectoryFactory.getInstance(project).createDirectory(virtualFile)
        files.forEach {
            CodeStyleManager.getInstance(project).reformat(it)
        }

        application.runWriteAction {
            files.forEach {
                try {
                    directory.add(it)
                } catch (ex: Exception) {
                    println("Can't add file ${it.name} $ex")
                }
            }
        }
    }
}