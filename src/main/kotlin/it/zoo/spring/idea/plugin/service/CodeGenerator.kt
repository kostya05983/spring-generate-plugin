package it.zoo.spring.idea.plugin.service

import it.zoo.spring.idea.plugin.model.*
import java.lang.StringBuilder

class CodeGenerator {
    fun formString(converter: Converter, pack: String): String {
        val sb = StringBuilder()
        sb.append("package $pack\n")
        sb.append("import org.springframework.core.convert.converter.Converter\n")
        converter.imports.forEach {
            sb.append("import $it\n")
        }
        sb.append("object ${converter.name}: Converter<${converter.from}, ${converter.to}>{\n")
        sb.append("override fun convert(source: ${converter.from}): ${converter.to} {\n")
        when (converter) {
            is ClassConverter -> {
                sb.append("return ${converter.to}(\n")

                for (it in converter.elements) {
                    when (it.type) {
                        ConvertedElement.Type.SIMPLE -> sb.append("${it.from} = source.${it.to}")
                        ConvertedElement.Type.NULLABLE_CONVERT -> sb.append("${it.from}=source.${it.to}?.let{${it.toType}Converter.convert(it)}")
                        ConvertedElement.Type.CONVERT -> sb.append("${it.from}=${it.toType}Converter.convert(source.${it.to})")
                        ConvertedElement.Type.LIST_CONVERT -> sb.append("${it.from}=source.${it.to}?.map{ ${it.toType}Converter.convert(it) }")
                        ConvertedElement.Type.NULLABLE_LIST_CONVERT -> sb.append("${it.from}=source.${it.to}?.map{ ${it.toType}Converter.convert(it) }")
                    }
                    if (it == converter.elements.last()) {
                        sb.append("\n")
                    } else {
                        sb.append(",\n")
                    }
                }
                sb.append(")\n")
            }
            is EnumClassConverter -> {
                sb.append("return when(source) {")
                for (it in converter.elements)
                    sb.append("${it.from} -> ${it.to}\n")
                sb.append("}\n")
            }
            is SealedClassConverter -> {
                sb.append("return when(source) {")
                for (it in converter.elements) {
                    sb.append("is ${it.from} -> ${it.to}Converter.convert(source)\n")
                }
                sb.append("}\n")
            }
        }
        sb.append("}\n")
        sb.append("}\n")
        return sb.toString()
    }
}