package ast;

import java.io.StringWriter;

public class LiteralOperator extends ParentNode {
    public LiteralOperator(BaseNode child) {
        super(NodeType.LiteralOperator, child);
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("operator \"");
        child.PrintLeft(writer);
        writer.write("\"");
    }
}