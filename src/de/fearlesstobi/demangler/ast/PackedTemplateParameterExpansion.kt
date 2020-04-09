package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class PackedTemplateParameterExpansion(child: BaseNode?) : ParentNode(NodeType.PackedTemplateParameterExpansion, child) {
    public override fun printLeft(writer: StringWriter) {
        if (child is PackedTemplateParameter) {
            if (!child.nodes!!.isEmpty()) {
                child.print(writer)
            }
        } else {
            writer.write("...")
        }
    }
}