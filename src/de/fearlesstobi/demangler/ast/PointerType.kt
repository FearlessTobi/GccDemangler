package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class PointerType(private val child: BaseNode) : BaseNode(NodeType.PointerType) {
    public override fun hasRightPart(): Boolean {
        return child.hasRightPart()
    }

    public override fun printLeft(writer: StringWriter) {
        child.printLeft(writer)
        if (child.isArray) {
            writer.write(" ")
        }
        if (child.isArray || child.hasFunctions()) {
            writer.write("(")
        }
        writer.write("*")
    }

    public override fun printRight(writer: StringWriter) {
        if (child.isArray || child.hasFunctions()) {
            writer.write(")")
        }
        child.printRight(writer)
    }

}