package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class ConditionalExpression(private val conditionNode: BaseNode, private val thenNode: BaseNode, private val elseNode: BaseNode) : BaseNode(NodeType.ConditionalExpression) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("(")
        conditionNode.print(writer)
        writer.write(") ? (")
        thenNode.print(writer)
        writer.write(") : (")
        elseNode.print(writer)
        writer.write(")")
    }

}