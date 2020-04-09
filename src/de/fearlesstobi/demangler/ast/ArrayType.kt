package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class ArrayType : BaseNode {
    private val base: BaseNode
    private var dimensionExpression: BaseNode? = null
    private var dimensionString: String? = null

    constructor(base: BaseNode) : super(NodeType.ArrayType) {
        this.base = base
        dimensionExpression = null
    }

    constructor(base: BaseNode, dimensionExpression: BaseNode?) : super(NodeType.ArrayType) {
        this.base = base
        this.dimensionExpression = dimensionExpression
    }

    constructor(base: BaseNode, dimensionString: String?) : super(NodeType.ArrayType) {
        this.base = base
        this.dimensionString = dimensionString
    }

    public override fun hasRightPart(): Boolean {
        return true
    }

    override val isArray: Boolean
        get() = true

    public override fun printLeft(writer: StringWriter) {
        base.printLeft(writer)
    }

    public override fun printRight(writer: StringWriter) { // FIXME: detect if previous char was a ].
        writer.write(" ")
        writer.write("[")
        if (dimensionString != null) {
            writer.write(dimensionString)
        } else if (dimensionExpression != null) {
            dimensionExpression!!.print(writer)
        }
        writer.write("]")
        base.printRight(writer)
    }
}