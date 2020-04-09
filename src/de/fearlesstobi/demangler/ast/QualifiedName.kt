package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class QualifiedName(private val qualifier: BaseNode?, val name: BaseNode) : BaseNode(NodeType.QualifiedName) {
    public override fun printLeft(writer: StringWriter) {
        qualifier!!.print(writer)
        writer.write("::")
        name.print(writer)
    }

}