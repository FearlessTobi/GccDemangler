package de.fearlesstobi.demangler.util

import de.fearlesstobi.demangler.ast.BaseNode
import java.util.*

object StringUtil {
    fun nodeListToArray(nodes: List<BaseNode?>?): Array<String> {
        val nodeStrings: MutableList<String> = LinkedList()
        for (node in nodes!!) {
            nodeStrings.add(node.toString())
        }
        return nodeStrings.toTypedArray()
    }
}