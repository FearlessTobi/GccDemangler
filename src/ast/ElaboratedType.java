package ast;

import java.io.StringWriter;

public class ElaboratedType extends ParentNode {
    private String _elaborated;

    public ElaboratedType(String elaborated, BaseNode type) {
        super(NodeType.ElaboratedType, type);
        _elaborated = elaborated;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write(_elaborated);
        writer.write(" ");
        child.Print(writer);
    }
}