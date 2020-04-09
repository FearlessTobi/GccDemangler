package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class GlobalQualifiedName(child: BaseNode?) : ParentNode(NodeType.GlobalQualifiedName, child) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("::")
        child!!.print(writer)
    }
}