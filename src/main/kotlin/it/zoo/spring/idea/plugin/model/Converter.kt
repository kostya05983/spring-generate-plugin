package it.zoo.spring.idea.plugin.model

sealed class Converter {
    abstract val name: String
    abstract val from: String
    abstract val to: String
    abstract val imports: List<String>
}

data class DataClassConverter(
    override val name: String,
    override val from: String,
    override val to: String,
    override val imports: List<String>,
    val elements: List<ConvertedElement>
) : Converter()

data class EnumClassConverter(
    override val name: String,
    override val from: String,
    override val to: String,
    override val imports: List<String>,
    val elements: List<ConvertedElement>
) : Converter()

data class SealedClassConverter(
    override val name: String,
    override val from: String,
    override val to: String,
    override val imports: List<String>,
    val elements: List<ConvertedElement>
) : Converter()