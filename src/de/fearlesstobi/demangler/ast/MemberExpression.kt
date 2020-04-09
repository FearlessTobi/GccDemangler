package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class MemberExpression(private val leftNode: BaseNode, private val kind: String, private val rightNode: BaseNode) : BaseNode(NodeType.MemberExpression) {
    public override fun printLeft(writer: StringWriter) {
        leftNode.print(writer)
        writer.write(kind)
        rightNode.print(writer)
    }

}