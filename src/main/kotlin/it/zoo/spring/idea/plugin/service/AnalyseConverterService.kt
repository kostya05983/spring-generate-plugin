package it.zoo.spring.idea.plugin.service

import com.intellij.openapi.project.Project
import it.zoo.spring.idea.plugin.model.Converter
import it.zoo.spring.idea.plugin.model.DtoModelPair
import it.zoo.spring.idea.plugin.service.strategies.AnalyseClassStrategy
import it.zoo.spring.idea.plugin.service.strategies.EnumAnalyseStrategy
import it.zoo.spring.idea.plugin.service.strategies.SealedAnalyseStrategy
import org.jetbrains.kotlin.psi.KtClass
import java.util.*

class AnalyseConverterService(project: Project, generatorStyle: GeneratorStyle) {

    private val sealedAnalyseStrategy = SealedAnalyseStrategy(project, generatorStyle)
    private val classAnalyseStrategy = AnalyseClassStrategy(project, generatorStyle)
    private val enumAnalyseStrategy = EnumAnalyseStrategy(project, generatorStyle)

    fun analyse(model: KtClass, dto: KtClass): List<Converter> {
        val stack = Stack<DtoModelPair>()

        val pair = DtoModelPair(dto, model)
        stack.push(pair)

        val result = mutableListOf<Converter>()
        while (stack.isNotEmpty()) {
            val pair = stack.pop()
            val model = pair.model
            val dto = pair.dto
            when {
                dto.isData() -> {
                    val converter = classAnalyseStrategy.analyse(model, dto, stack)
                    result.add(converter)
                }

                dto.isEnum() -> {
                    val converter = enumAnalyseStrategy.analyse(model, dto, stack)
                    result.add(converter)
                }

                dto.isSealed() -> {
                    val converter = sealedAnalyseStrategy.analyse(model, dto, stack)
                    result.add(converter)
                }

                else -> {
                    val converter = classAnalyseStrategy.analyse(model, dto, stack)
                    result.add(converter)
                }
            }
        }
        return result
    }

}