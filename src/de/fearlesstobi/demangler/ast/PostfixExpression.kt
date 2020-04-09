package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class PostfixExpression(type: BaseNode?, private val operator: String) : ParentNode(NodeType.PostfixExpression, type) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("(")
        child!!.print(writer)
        writer.write(")")
        writer.write(operator)
    }

}