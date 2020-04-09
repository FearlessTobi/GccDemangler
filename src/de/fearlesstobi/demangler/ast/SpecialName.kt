package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class SpecialName(private val specialValue: String, type: BaseNode?) : ParentNode(NodeType.SpecialName, type) {
    public override fun printLeft(writer: StringWriter) {
        writer.write(specialValue)
        child!!.print(writer)
    }

}