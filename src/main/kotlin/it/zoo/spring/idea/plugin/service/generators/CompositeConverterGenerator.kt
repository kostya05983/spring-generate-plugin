package it.zoo.spring.idea.plugin.service.generators

import it.zoo.spring.idea.plugin.model.Converter
import it.zoo.spring.idea.plugin.service.GeneratorStyle

class CompositeConverterGenerator(
    generatorStyle: GeneratorStyle
) : ConverterGenerator {
    private val generator = when (generatorStyle) {
        GeneratorStyle.SPRING -> SpringConvertersGenerator()
        GeneratorStyle.KOTLIN -> KotlinConvertersGenerator()
    }

    override fun getString(converters: List<Converter>, packageName: String): String {
        return generator.getString(converters, packageName,)
    }
}