package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class NameType : BaseNode {
    private val nameValue: String

    constructor(nameValue: String, type: NodeType) : super(type) {
        this.nameValue = nameValue
    }

    constructor(nameValue: String) : super(NodeType.NameType) {
        this.nameValue = nameValue
    }

    override fun getName(): String? {
        return nameValue
    }

    public override fun printLeft(writer: StringWriter) {
        writer.write(nameValue)
    }
}