package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class CastExpression(private val kind: String, private val to: BaseNode, private val from: BaseNode) : BaseNode(NodeType.CastExpression) {
    public override fun printLeft(writer: StringWriter) {
        writer.write(kind)
        writer.write("<")
        to.printLeft(writer)
        writer.write(">(")
        from.printLeft(writer)
        writer.write(")")
    }

}