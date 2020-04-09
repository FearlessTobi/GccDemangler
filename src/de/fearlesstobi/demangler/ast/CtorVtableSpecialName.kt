package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class CtorVtableSpecialName(private val firstType: BaseNode?, private val secondType: BaseNode) : BaseNode(NodeType.CtorVtableSpecialName) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("construction vtable for ")
        firstType!!.print(writer)
        writer.write("-in-")
        secondType.print(writer)
    }

}