package ast;

import java.io.StringWriter;

public class PrefixExpression extends ParentNode {
    private String _prefix;

    public PrefixExpression(String prefix, BaseNode child) {
        super(NodeType.PrefixExpression, child);
        _prefix = prefix;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write(_prefix);
        writer.write("(");
        child.Print(writer);
        writer.write(")");
    }
}