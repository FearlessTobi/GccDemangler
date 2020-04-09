package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class EncodedFunction(val name: BaseNode, private val params: BaseNode?, private val cv: BaseNode?, private val ref: BaseNode?, private val attrs: BaseNode?, private val ret: BaseNode?) : BaseNode(NodeType.NameType) {
    public override fun printLeft(writer: StringWriter) {
        if (ret != null) {
            ret.printLeft(writer)
            if (!ret.hasRightPart()) {
                writer.write(" ")
            }
        }
        name.print(writer)
    }

    public override fun hasRightPart(): Boolean {
        return true
    }

    public override fun printRight(writer: StringWriter) {
        writer.write("(")
        params?.print(writer)
        writer.write(")")
        ret?.printRight(writer)
        cv?.print(writer)
        ref?.print(writer)
        attrs?.print(writer)
    }
}