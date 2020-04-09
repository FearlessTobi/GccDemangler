package de.fearlesstobi.demangler.ast

import de.fearlesstobi.demangler.util.StringUtil
import java.io.StringWriter
import java.lang.String

class TemplateArguments(nodes: List<BaseNode?>?) : NodeArray(nodes, NodeType.TemplateArguments) {
    override fun printLeft(writer: StringWriter) {
        val params = String.join(", ", *StringUtil.nodeListToArray(nodes))
        writer.write("<")
        writer.write(params)
        if (params.endsWith(">")) {
            writer.write(" ")
        }
        writer.write(">")
    }
}