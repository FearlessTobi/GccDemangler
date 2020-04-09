package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class LiteralOperator(child: BaseNode?) : ParentNode(NodeType.LiteralOperator, child) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("operator \"")
        child!!.printLeft(writer)
        writer.write("\"")
    }
}