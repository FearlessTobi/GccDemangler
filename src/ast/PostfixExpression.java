package ast;

import java.io.StringWriter;

public class PostfixExpression extends ParentNode {
    private String _operator;

    public PostfixExpression(BaseNode type, String Operator) {
        super(NodeType.PostfixExpression, type);
        _operator = Operator;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("(");
        child.Print(writer);
        writer.write(")");
        writer.write(_operator);
    }
}