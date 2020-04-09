package ast;

import java.io.StringWriter;

public class ThrowExpression extends BaseNode {
    private BaseNode _expression;

    public ThrowExpression(BaseNode expression) {
        super(NodeType.ThrowExpression);
        _expression = expression;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("throw ");
        _expression.Print(writer);
    }
}