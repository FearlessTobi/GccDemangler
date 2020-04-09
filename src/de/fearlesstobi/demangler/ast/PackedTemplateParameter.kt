package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class PackedTemplateParameter(nodes: List<BaseNode?>?) : NodeArray(nodes, NodeType.PackedTemplateParameter) {
    override fun printLeft(writer: StringWriter) {
        for (node in nodes!!) {
            node!!.printLeft(writer)
        }
    }

    public override fun printRight(writer: StringWriter) {
        for (node in nodes!!) {
            node!!.printLeft(writer)
        }
    }

    public override fun hasRightPart(): Boolean {
        for (node in nodes!!) {
            if (node!!.hasRightPart()) {
                return true
            }
        }
        return false
    }
}