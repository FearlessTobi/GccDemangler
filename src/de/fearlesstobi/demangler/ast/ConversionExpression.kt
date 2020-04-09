package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class ConversionExpression(private val typeNode: BaseNode, private val expressions: BaseNode) : BaseNode(NodeType.ConversionExpression) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("(")
        typeNode.print(writer)
        writer.write(")(")
        expressions.print(writer)
    }

}