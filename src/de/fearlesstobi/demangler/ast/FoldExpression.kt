package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class FoldExpression(private val isLeftFold: Boolean, private val operatorName: String, private val expression: BaseNode, private val initializer: BaseNode?) : BaseNode(NodeType.FunctionParameter) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("(")
        if (isLeftFold && initializer != null) {
            initializer.print(writer)
            writer.write(" ")
            writer.write(operatorName)
            writer.write(" ")
        }
        writer.write(if (isLeftFold) "... " else " ")
        writer.write(operatorName)
        writer.write(if (!isLeftFold) " ..." else " ")
        expression.print(writer)
        if (!isLeftFold && initializer != null) {
            initializer.print(writer)
            writer.write(" ")
            writer.write(operatorName)
            writer.write(" ")
        }
        writer.write(")")
    }

}