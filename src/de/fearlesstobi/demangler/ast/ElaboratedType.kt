package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class ElaboratedType(private val elaborated: String, type: BaseNode?) : ParentNode(NodeType.ElaboratedType, type) {
    public override fun printLeft(writer: StringWriter) {
        writer.write(elaborated)
        writer.write(" ")
        child!!.print(writer)
    }

}