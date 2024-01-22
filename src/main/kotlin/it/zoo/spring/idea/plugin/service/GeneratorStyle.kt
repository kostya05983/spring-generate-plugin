package it.zoo.spring.idea.plugin.service

enum class GeneratorStyle {
    SPRING,
    KOTLIN;

    fun getFileName(prefix: String): String {
        return when (this) {
            GeneratorStyle.SPRING -> "${prefix}Converter"
            GeneratorStyle.KOTLIN -> "${prefix.replace("dto", "", ignoreCase = true)}Conversions"
        }
    }
}