package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class IntegerLiteral(private val _literalName: String, private val _literalValue: String) : BaseNode(NodeType.IntegerLiteral) {
    public override fun printLeft(writer: StringWriter) {
        if (_literalName.length > 3) {
            writer.write("(")
            writer.write(_literalName)
            writer.write(")")
        }
        if (_literalValue[0] == 'n') {
            writer.write("-")
            writer.write(_literalValue.substring(1))
        } else {
            writer.write(_literalValue)
        }
        if (_literalName.length <= 3) {
            writer.write(_literalName)
        }
    }

}