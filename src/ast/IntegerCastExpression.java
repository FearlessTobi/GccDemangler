package ast;

import java.io.StringWriter;

public class IntegerCastExpression extends ParentNode {
    private String _number;

    public IntegerCastExpression(BaseNode type, String number) {
        super(NodeType.IntegerCastExpression, type);
        _number = number;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("(");
        child.Print(writer);
        writer.write(")");
        writer.write(_number);
    }
}