package it.zoo.spring.idea.plugin.model

data class Converter(
    val name: String,
    val from: String,
    val to: String,
    val imports: List<String>,
    val elements: List<ConvertedElement>
)