package de.fearlesstobi.demangler.ast

import de.fearlesstobi.demangler.util.StringUtil
import java.io.StringWriter

class InitListExpression(private val typeNode: BaseNode?, private val nodes: List<BaseNode?>) : BaseNode(NodeType.InitListExpression) {
    public override fun printLeft(writer: StringWriter) {
        typeNode?.print(writer)
        writer.write("{")
        writer.write(StringUtil.nodeListToArray(nodes).joinToString { ", " })
        writer.write("}")
    }

}