package de.fearlesstobi.demangler.ast

abstract class ParentNode(type: NodeType, val child: BaseNode?) : BaseNode(type) {
    override fun getName(): String?{
        return child?.getName()
    }

}