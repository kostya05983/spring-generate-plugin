package it.zoo.spring.idea.plugin.model

sealed class ConvertedElement {
    abstract val from: String?
    abstract val to: String?

    abstract fun reverse(): ConvertedElement
}

data class SimpleConvertedElement(
    override val from: String?,
    override val to: String?
) : ConvertedElement() {
    override fun reverse(): ConvertedElement {
        return SimpleConvertedElement(to, from)
    }
}

data class ConvertConvertedElement(
    val isNullableConvert: Boolean,
    override val from: String,
    override val to: String,
    val toType: String?
) : ConvertedElement() {
    override fun reverse(): ConvertedElement {
        return ConvertConvertedElement(isNullableConvert, to, from, toType)
    }
}

data class ListConvertedElement(
    val isNullableConvert: Boolean,
    override val from: String,
    override val to: String,
    val element: ConvertedElement
) : ConvertedElement() {
    override fun reverse(): ConvertedElement {
        return ListConvertedElement(isNullableConvert, to, from, element)
    }
}