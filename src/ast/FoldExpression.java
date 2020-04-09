package ast;

import java.io.StringWriter;

public class FoldExpression extends BaseNode {
    private boolean _isLeftFold;
    private String _operatorName;
    private BaseNode _expression;
    private BaseNode _initializer;

    public FoldExpression(boolean isLeftFold, String operatorName, BaseNode expression, BaseNode initializer) {
        super(NodeType.FunctionParameter);
        _isLeftFold = isLeftFold;
        _operatorName = operatorName;
        _expression = expression;
        _initializer = initializer;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("(");

        if (_isLeftFold && _initializer != null) {
            _initializer.Print(writer);
            writer.write(" ");
            writer.write(_operatorName);
            writer.write(" ");
        }

        writer.write(_isLeftFold ? "... " : " ");
        writer.write(_operatorName);
        writer.write(!_isLeftFold ? " ..." : " ");
        _expression.Print(writer);

        if (!_isLeftFold && _initializer != null) {
            _initializer.Print(writer);
            writer.write(" ");
            writer.write(_operatorName);
            writer.write(" ");
        }

        writer.write(")");
    }
}