package ast;

import java.io.StringWriter;

public class BracedRangeExpression extends BaseNode {
    private BaseNode _firstNode;
    private BaseNode _lastNode;
    private BaseNode _expression;

    public BracedRangeExpression(BaseNode firstNode, BaseNode lastNode, BaseNode expression) {
        super(NodeType.BracedRangeExpression);
        _firstNode = firstNode;
        _lastNode = lastNode;
        _expression = expression;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("[");
        _firstNode.Print(writer);
        writer.write(" ... ");
        _lastNode.Print(writer);
        writer.write("]");

        if (!(_expression instanceof BracedExpression) || !(_expression instanceof BracedRangeExpression)) {
            writer.write(" = ");
        }

        _expression.Print(writer);
    }
}
