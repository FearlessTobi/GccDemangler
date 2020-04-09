package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class NewExpression(private val expressions: NodeArray, private val typeNode: BaseNode, private val initializers: NodeArray, private val isGlobal: Boolean, private val isArrayExpression: Boolean) : BaseNode(NodeType.NewExpression) {
    public override fun printLeft(writer: StringWriter) {
        if (isGlobal) {
            writer.write("::operator ")
        }
        writer.write("new ")
        if (isArrayExpression) {
            writer.write("[] ")
        }
        if (!expressions.nodes!!.isEmpty()) {
            writer.write("(")
            expressions.print(writer)
            writer.write(")")
        }
        typeNode.print(writer)
        if (!initializers.nodes!!.isEmpty()) {
            writer.write("(")
            initializers.print(writer)
            writer.write(")")
        }
    }

}