package it.zoo.spring.idea.plugin.service.generators

import it.zoo.spring.idea.plugin.model.*

class KotlinConvertersGenerator : ConverterGenerator {

    override fun getString(converters: List<Converter>, packageName: String): String {
        val sb = StringBuilder()
        sb.append("package $packageName\n")
        converters.flatMap { it.imports }.toSet().forEach {
            sb.append("import $it\n")
        }

        for (converter in converters) {
            val postfix = if (converter.to.lowercase().contains("dto")) {
                "Dto"
            } else {
                "Model"
            }

            getBody(sb, converter, postfix)
        }

        return sb.toString()
    }

    private fun getBody(sb: StringBuilder, converter: Converter, postfix: String) {
        sb.append("fun ${converter.from}.to$postfix(): ${converter.to} {\n")
        when (converter) {
            is ClassConverter -> {
                sb.append("return ${converter.to}(\n")
                for (it in converter.elements) {
                    when (it) {
                        is SimpleConvertedElement -> {
                            if (it.to == null) {
                                sb.append("${it.from} = TODO()")
                            } else {
                                sb.append("${it.from} = ${it.to}")
                            }
                        }

                        is ConvertConvertedElement -> {
                            if (it.isNullableConvert) {
                                if (it.toType != null) {
                                    sb.append("${it.from} = ${it.to}?.to$postfix()")
                                } else {
                                    sb.append("${it.from} = TODO()")
                                }

                            } else {
                                if (it.toType != null) {
                                    sb.append("${it.from} = ${it.to}.to$postfix()")
                                } else {
                                    sb.append("${it.from} = TODO()")
                                }
                            }
                        }

                        is ListConvertedElement -> {
                            if (it.isNullableConvert) {
                                sb.append("${it.from}=${it.to}?.map{ ")
                            } else {
                                sb.append("${it.from}=${it.to}.map{ ")
                            }
                            val element = it.element as ConvertConvertedElement
                            if (element.isNullableConvert) {
                                sb.append("it?.to$postfix() }")
                            } else {
                                sb.append("it.to$postfix() }")
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
                sb.append("return when(this) {\n")
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
                sb.append("return when(this) {\n")
                for (it in converter.elements) {
                    if (it.to != null) {
                        sb.append("is ${it.from} -> this.to$postfix()\n")
                    } else {
                        sb.append("is ${it.from} -> TODO()\n")
                    }
                }
                sb.append("}\n")
            }
        }
        sb.append("}\n")
    }
}