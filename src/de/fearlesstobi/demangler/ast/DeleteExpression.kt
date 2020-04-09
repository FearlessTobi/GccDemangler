package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class DeleteExpression(child: BaseNode?, private val isGlobal: Boolean, private val isArrayExpression: Boolean) : ParentNode(NodeType.DeleteExpression, child) {
    public override fun printLeft(writer: StringWriter) {
        if (isGlobal) {
            writer.write("::")
        }
        writer.write("delete")
        if (isArrayExpression) {
            writer.write("[] ")
        }
        child!!.print(writer)
    }

}