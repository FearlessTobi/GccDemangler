package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class BinaryExpression(private val leftPart: BaseNode, val _name: String, private val rightPart: BaseNode) : BaseNode(NodeType.BinaryExpression) {
    public override fun printLeft(writer: StringWriter) {
        if (_name == ">") {
            writer.write("(")
        }
        writer.write("(")
        leftPart.print(writer)
        writer.write(") ")
        writer.write(_name)
        writer.write(" (")
        rightPart.print(writer)
        writer.write(")")
        if (_name == ">") {
            writer.write(")")
        }
    }

    override fun getName(): String? {
        return _name
    }
}