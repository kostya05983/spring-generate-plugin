package it.zoo.spring.idea.plugin.model

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