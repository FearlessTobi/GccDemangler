package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class CtorDtorNameType(name: BaseNode?, private val isDestructor: Boolean) : ParentNode(NodeType.CtorDtorNameType, name) {
    public override fun printLeft(writer: StringWriter) {
        if (isDestructor) {
            writer.write("~")
        }
        writer.write(child!!.getName())
    }

}