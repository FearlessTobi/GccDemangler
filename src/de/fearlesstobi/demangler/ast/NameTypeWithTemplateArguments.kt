package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class NameTypeWithTemplateArguments(private val prev: BaseNode, private val templateArgument: BaseNode) : BaseNode(NodeType.NameTypeWithTemplateArguments) {
    override fun getName(): String? {
        return prev.getName()
    }

    public override fun printLeft(writer: StringWriter) {
        prev.print(writer)
        templateArgument.print(writer)
    }

}