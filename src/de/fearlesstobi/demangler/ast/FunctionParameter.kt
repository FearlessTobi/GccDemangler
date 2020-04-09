package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class FunctionParameter(private val number: String?) : BaseNode(NodeType.FunctionParameter) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("fp ")
        if (number != null) {
            writer.write(number)
        }
    }

}