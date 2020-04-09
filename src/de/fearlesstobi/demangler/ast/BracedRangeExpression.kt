package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class BracedRangeExpression(private val firstNode: BaseNode, private val lastNode: BaseNode, private val expression: BaseNode) : BaseNode(NodeType.BracedRangeExpression) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("[")
        firstNode.print(writer)
        writer.write(" ... ")
        lastNode.print(writer)
        writer.write("]")
        writer.write(" = ")
        expression.print(writer)
    }

}