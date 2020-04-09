package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class DynamicExceptionSpec(child: BaseNode?) : ParentNode(NodeType.DynamicExceptionSpec, child) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("throw(")
        child!!.print(writer)
        writer.write(")")
    }
}