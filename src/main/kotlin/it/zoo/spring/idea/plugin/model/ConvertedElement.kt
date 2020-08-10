package it.zoo.spring.idea.plugin.model

sealed class ConvertedElement {
    abstract val from: String
    abstract val to: String
}

data class SimpleConvertedElement(
    override val from: String,
    override val to: String
) : ConvertedElement()

data class ConvertConvertedElement(
    val isNullableConvert: Boolean,
    override val from: String,
    override val to: String,
    val toType: String
) : ConvertedElement()

data class ListConvertedElement(
    val isNullableConvert: Boolean,
    override val from: String,
    override val to: String,
    val element: ConvertedElement
) : ConvertedElement()