package it.zoo.spring.idea.plugin.service

import it.zoo.spring.idea.plugin.model.ConvertedElement
import it.zoo.spring.idea.plugin.model.Converter
import java.lang.StringBuilder

class CodeGenerator {
    fun formString(converter: Converter): String {
        val sb = StringBuilder()
        sb.append("import org.springframework.core.convert.converter.Converter\n")
        converter.imports.forEach {
            sb.append("import $it\n")
        }
        sb.append("object ${converter.name}: Converter<${converter.from}, ${converter.to}>{\n")
        sb.append("override fun convert(source: ${converter.from}): ${converter.to} {\n")
        when (converter.typeClass) {
            Converter.TypeClass.DATA -> {
                sb.append("return ${converter.to}(\n")

                for (it in converter.elements) {
                    when (it.type) {
                        ConvertedElement.Type.SIMPLE -> sb.append("${it.from} = source.${it.to}")
                        ConvertedElement.Type.NULLABLE_CONVERT -> sb.append("${it.from}=source.${it.to}?.let{${it.toType}Converter.convert(it)}")
                        ConvertedElement.Type.CONVERT -> sb.append("${it.from}=${it.toType}Converter.convert(source.${it.to})")
                        ConvertedElement.Type.LIST_CONVERT -> sb.append("${it.from}=${it.to}?.map{ ${it.toType}Converter.convert(it) }")
                        ConvertedElement.Type.NULLABLE_LIST_CONVERT -> sb.append("${it.from}=${it.to}?.map{ it?.let{${it.toType}Converter.convert(it) }}")
                    }
                    if (it == converter.elements.last()) {
                        sb.append("\n")
                    } else {
                        sb.append(",\n")
                    }
                }
                sb.append(")\n")
            }
            Converter.TypeClass.ENUM -> {
                sb.append("return when(source) {")
                for (it in converter.elements)
                    sb.append("${it.from} -> ${it.to}\n")
                sb.append("}\n")
            }
        }
        sb.append("}\n")
        sb.append("}\n")
        return sb.toString()
    }
}