package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class BracedExpression(private val element: BaseNode, private val expression: BaseNode, private val isArrayExpression: Boolean) : BaseNode(NodeType.BracedExpression) {
    public override fun printLeft(writer: StringWriter) {
        if (isArrayExpression) {
            writer.write("[")
            element.print(writer)
            writer.write("]")
        } else {
            writer.write(".")
            element.print(writer)
        }
        writer.write(" = ")
        expression.print(writer)
    }

}