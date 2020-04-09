package ast;

import java.io.StringWriter;

public class BracedExpression extends BaseNode {
    private BaseNode _element;
    private BaseNode _expression;
    private boolean _isArrayExpression;

    public BracedExpression(BaseNode element, BaseNode expression, boolean isArrayExpression) {
        super(NodeType.BracedExpression);
        _element = element;
        _expression = expression;
        _isArrayExpression = isArrayExpression;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (_isArrayExpression) {
            writer.write("[");
            _element.Print(writer);
            writer.write("]");
        } else {
            writer.write(".");
            _element.Print(writer);
        }

        if (!(_expression instanceof BracedExpression) || !(_expression instanceof BracedRangeExpression)) {
            writer.write(" = ");
        }

        _expression.Print(writer);
    }
}

