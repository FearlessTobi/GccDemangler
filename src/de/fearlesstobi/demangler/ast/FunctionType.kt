package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class FunctionType(private val returnType: BaseNode, private val params: BaseNode, private val cvQualifier: BaseNode, private val referenceQualifier: SimpleReferenceType, private val exceptionSpec: BaseNode?) : BaseNode(NodeType.FunctionType) {
    public override fun printLeft(writer: StringWriter) {
        returnType.printLeft(writer)
        writer.write(" ")
    }

    public override fun printRight(writer: StringWriter) {
        writer.write("(")
        params.print(writer)
        writer.write(")")
        returnType.printRight(writer)
        cvQualifier.print(writer)
        if (referenceQualifier.qualifier != Reference.None) {
            writer.write(" ")
            referenceQualifier.PrintQualifier(writer)
        }
        if (exceptionSpec != null) {
            writer.write(" ")
            exceptionSpec.print(writer)
        }
    }

    public override fun hasRightPart(): Boolean {
        return true
    }

    public override fun hasFunctions(): Boolean {
        return true
    }

}