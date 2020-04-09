package de.fearlesstobi.demangler.ast

import de.fearlesstobi.demangler.util.StringUtil
import java.io.StringWriter

open class NodeArray : BaseNode {
    val nodes: List<BaseNode?>?

    constructor(nodes: List<BaseNode?>?) : super(NodeType.NodeArray) {
        this.nodes = nodes
    }

    constructor(nodes: List<BaseNode?>?, type: NodeType) : super(type) {
        this.nodes = nodes
    }

    override val isArray: Boolean
        get() = true

    public override fun printLeft(writer: StringWriter) {
        writer.write(StringUtil.nodeListToArray(nodes).joinToString { ", " })
    }
}