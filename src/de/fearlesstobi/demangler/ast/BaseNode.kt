package de.fearlesstobi.demangler.ast

import java.io.StringWriter

abstract class BaseNode internal constructor(var type: NodeType) {
    //virtual
    fun print(writer: StringWriter) {
        printLeft(writer)
        if (hasRightPart()) {
            printRight(writer)
        }
    }

    abstract fun printLeft(writer: StringWriter)
    //virtual
    open fun hasRightPart(): Boolean {
        return false
    }

    //virtual
    open val isArray: Boolean
        get() = false

    //virtual
    open fun hasFunctions(): Boolean {
        return false
    }

    //virtual
    open fun getName(): String?{
        return ""
    }

    //virtual
    open fun printRight(writer: StringWriter) {}

    override fun toString(): String {
        val writer = StringWriter()
        print(writer)
        return writer.toString()
    }

}