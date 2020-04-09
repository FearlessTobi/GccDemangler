package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class SimpleReferenceType(val qualifier: Int, child: BaseNode?) : ParentNode(NodeType.SimpleReferenceType, child) {
    fun PrintQualifier(writer: StringWriter) {
        if (qualifier and Reference.LValue != 0) {
            writer.write("&")
        }
        if (qualifier and Reference.RValue != 0) {
            writer.write("&&")
        }
    }

    public override fun printLeft(writer: StringWriter) {
        if (child != null) {
            child.printLeft(writer)
        } else if (qualifier != Reference.None) {
            writer.write(" ")
        }
        PrintQualifier(writer)
    }

    public override fun hasRightPart(): Boolean {
        return child != null && child.hasRightPart()
    }

    public override fun printRight(writer: StringWriter) {
        child?.printRight(writer)
    }

}