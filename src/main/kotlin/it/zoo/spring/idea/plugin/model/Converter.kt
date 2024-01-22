package it.zoo.spring.idea.plugin.model

sealed class Converter {
    abstract val name: String
    abstract val from: String
    abstract val to: String
    abstract val imports: List<String>

    abstract fun reverse(): Converter
}

data class ClassConverter(
    override val name: String,
    override val from: String,
    override val to: String,
    override val imports: List<String>,
    val elements: List<ConvertedElement>
) : Converter() {
    override fun reverse(): Converter {
        return ClassConverter(
            name = name,
            from = to,
            to = from,
            imports = imports,
            elements = elements.map { it.reverse() }
        )
    }
}

data class EnumClassConverter(
    override val name: String,
    override val from: String,
    override val to: String,
    override val imports: List<String>,
    val elements: List<ConvertedElement>
) : Converter() {
    override fun reverse(): Converter {
        return EnumClassConverter(name, to, from, imports, elements.map { it.reverse() })
    }
}

data class SealedClassConverter(
    override val name: String,
    override val from: String,
    override val to: String,
    override val imports: List<String>,
    val elements: List<ConvertedElement>
) : Converter() {
    override fun reverse(): Converter {
        return SealedClassConverter(name, to, from, imports, elements.map { it.reverse() })
    }
}