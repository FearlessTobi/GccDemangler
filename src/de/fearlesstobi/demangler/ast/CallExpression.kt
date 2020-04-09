package de.fearlesstobi.demangler.ast

import de.fearlesstobi.demangler.util.StringUtil
import java.io.StringWriter

class CallExpression(private val callee: BaseNode, nodes: List<BaseNode?>?) : NodeArray(nodes, NodeType.CallExpression) {
    override fun printLeft(writer: StringWriter) {
        callee.print(writer)
        writer.write("(")
        writer.write(StringUtil.nodeListToArray(nodes).joinToString { ", " })
        writer.write(")")
    }

}