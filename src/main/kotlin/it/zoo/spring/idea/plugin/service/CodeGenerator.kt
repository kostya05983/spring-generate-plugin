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
                    when (it) {
                        is SimpleConvertedElement -> {
                            if (it.to == null) {
                                sb.append("${it.from} = TODO()")
                            } else {
                                sb.append("${it.from} = source.${it.to}")
                            }
                        }
                        is ConvertConvertedElement -> {
                            if (it.isNullableConvert) {
                                if (it.toType != null) {
                                    sb.append("${it.from} = source.${it.to}?.let{${it.toType}Converter.convert(it)}")
                                } else {
                                    sb.append("${it.from} = source.${it.to}?.let{ TODO() }")
                                }

                            } else {
                                if (it.toType != null) {
                                    sb.append("${it.from} = ${it.toType}Converter.convert(source.${it.to})")
                                } else {
                                    sb.append("${it.from} = TODO()")
                                }
                            }
                        }
                        is ListConvertedElement -> {
                            if (it.isNullableConvert) {
                                sb.append("${it.from}=source.${it.to}?.map{")
                            } else {
                                sb.append("${it.from}=source.${it.to}.map{")
                            }
                            val element = it.element as ConvertConvertedElement
                            if (element.isNullableConvert) {
                                sb.append("it?.let{${element.toType}Converter.convert(it)}}")
                            } else {
                                sb.append("${element.toType}Converter.convert(it)}")
                            }
                        }
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
                for (it in converter.elements) {
                    if (it.to != null) {
                        sb.append("${it.from} -> ${it.to}\n")
                    } else {
                        sb.append("${it.from} -> TODO()\n")
                    }
                }
                sb.append("}\n")
            }
            is SealedClassConverter -> {
                sb.append("return when(source) {")
                for (it in converter.elements) {
                    if (it.to != null) {
                        sb.append("is ${it.from} -> ${it.to}Converter.convert(source)\n")
                    } else {
                        sb.append("is ${it.from} -> TODO()\n")
                    }
                }
                sb.append("}\n")
            }
        }
        sb.append("}\n")
        sb.append("}\n")
        return sb.toString()
    }
}