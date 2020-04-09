package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class NestedName(val name: BaseNode, type: BaseNode?) : ParentNode(NodeType.NestedName, type) {
    override fun getName(): String? {
        return name.getName()
    }

    public override fun printLeft(writer: StringWriter) {
        child!!.print(writer)
        writer.write("::")
        name.print(writer)
    }

}