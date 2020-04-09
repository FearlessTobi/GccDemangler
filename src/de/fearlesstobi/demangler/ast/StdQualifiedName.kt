package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class StdQualifiedName(child: BaseNode?) : ParentNode(NodeType.StdQualifiedName, child) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("std::")
        child!!.print(writer)
    }
}