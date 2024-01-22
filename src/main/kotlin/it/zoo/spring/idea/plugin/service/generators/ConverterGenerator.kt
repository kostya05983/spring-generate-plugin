package it.zoo.spring.idea.plugin.service.generators

import it.zoo.spring.idea.plugin.model.Converter

interface ConverterGenerator {
    fun getString(converter: Converter, packageName: String): String
}