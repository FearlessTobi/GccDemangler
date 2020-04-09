package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class LocalName(private val encoding: BaseNode, private val entity: BaseNode) : BaseNode(NodeType.LocalName) {
    public override fun printLeft(writer: StringWriter) {
        encoding.print(writer)
        writer.write("::")
        entity.print(writer)
    }

}