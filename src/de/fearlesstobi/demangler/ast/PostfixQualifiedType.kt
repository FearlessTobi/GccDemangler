package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class PostfixQualifiedType(private val postfixQualifier: String, type: BaseNode?) : ParentNode(NodeType.PostfixQualifiedType, type) {
    public override fun printLeft(writer: StringWriter) {
        child!!.print(writer)
        writer.write(postfixQualifier)
    }

}