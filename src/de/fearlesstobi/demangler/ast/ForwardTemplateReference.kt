package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class ForwardTemplateReference : BaseNode(NodeType.ForwardTemplateReference) {
    // TODO: Compute inside the de.fearlesstobi.demangler.Demangler
    private val reference: BaseNode? = null

    override fun getName(): String? {
        return reference?.getName()
    }

    public override fun printLeft(writer: StringWriter) {
        reference!!.printLeft(writer)
    }

    public override fun printRight(writer: StringWriter) {
        reference!!.printRight(writer)
    }

    public override fun hasRightPart(): Boolean {
        return reference!!.hasRightPart()
    }
}