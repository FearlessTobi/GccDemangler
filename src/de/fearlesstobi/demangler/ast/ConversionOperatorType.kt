package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class ConversionOperatorType(child: BaseNode?) : ParentNode(NodeType.ConversionOperatorType, child) {
    public override fun printLeft(writer: StringWriter) {
        writer.write("operator ")
        child!!.print(writer)
    }
}