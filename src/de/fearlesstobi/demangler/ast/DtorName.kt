package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class DtorName(name: BaseNode?) : ParentNode(NodeType.DtOrName, name) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("~")
        child!!.printLeft(writer)
    }
}