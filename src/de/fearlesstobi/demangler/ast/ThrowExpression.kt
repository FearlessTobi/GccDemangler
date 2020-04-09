package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class ThrowExpression(private val expression: BaseNode) : BaseNode(NodeType.ThrowExpression) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("throw ")
        expression.print(writer)
    }

}