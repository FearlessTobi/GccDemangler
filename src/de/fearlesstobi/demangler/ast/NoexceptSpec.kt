package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class NoexceptSpec(child: BaseNode?) : ParentNode(NodeType.NoexceptSpec, child) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("noexcept(")
        child!!.print(writer)
        writer.write(")")
    }
}