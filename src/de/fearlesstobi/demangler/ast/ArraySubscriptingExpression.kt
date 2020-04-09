package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class ArraySubscriptingExpression(private val leftNode: BaseNode, private val subscript: BaseNode?) : BaseNode(NodeType.ArraySubscriptingExpression) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("(")
        leftNode.print(writer)
        writer.write(")[")
        subscript!!.print(writer)
        writer.write("]")
    }

}