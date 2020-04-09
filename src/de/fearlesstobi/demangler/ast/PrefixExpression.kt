package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class PrefixExpression(private val prefix: String, child: BaseNode?) : ParentNode(NodeType.PrefixExpression, child) {
    public override fun printLeft(writer: StringWriter) {
        writer.write(prefix)
        writer.write("(")
        child!!.print(writer)
        writer.write(")")
    }

}