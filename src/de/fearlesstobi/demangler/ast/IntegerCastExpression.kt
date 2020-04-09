package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class IntegerCastExpression(type: BaseNode?, private val number: String) : ParentNode(NodeType.IntegerCastExpression, type) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("(")
        child!!.print(writer)
        writer.write(")")
        writer.write(number)
    }

}