package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class CvType(private val qualifier: Int, child: BaseNode?) : ParentNode(NodeType.CvQualifierType, child) {
    object Cv {
        const val None = 0
        const val Const = 1
        const val Volatile = 2
        const val Restricted = 4
    }

    private fun PrintQualifier(writer: StringWriter) {
        if (qualifier and Cv.Const != 0) {
            writer.write(" const")
        }
        if (qualifier and Cv.Volatile != 0) {
            writer.write(" volatile")
        }
        if (qualifier and Cv.Restricted != 0) {
            writer.write(" restrict")
        }
    }

    public override fun printLeft(writer: StringWriter) {
        child?.printLeft(writer)
        PrintQualifier(writer)
    }

    public override fun hasRightPart(): Boolean {
        return child != null && child.hasRightPart()
    }

    public override fun printRight(writer: StringWriter) {
        child?.printRight(writer)
    }

}