package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class EnclosedExpression(private val prefix: String, private val expression: BaseNode, private val postfix: String) : BaseNode(NodeType.EnclosedExpression) {
    public override fun printLeft(writer: StringWriter) {
        writer.write(prefix)
        expression.print(writer)
        writer.write(postfix)
    }

}