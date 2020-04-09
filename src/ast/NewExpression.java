package ast;

import java.io.StringWriter;

public class NewExpression extends BaseNode {
    private NodeArray _expressions;
    private BaseNode _typeNode;
    private NodeArray _initializers;

    private boolean _isGlobal;
    private boolean _isArrayExpression;

    public NewExpression(NodeArray expressions, BaseNode typeNode, NodeArray initializers, boolean isGlobal, boolean isArrayExpression) {
        super(NodeType.NewExpression);
        _expressions = expressions;
        _typeNode = typeNode;
        _initializers = initializers;

        _isGlobal = isGlobal;
        _isArrayExpression = isArrayExpression;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (_isGlobal) {
            writer.write("::operator ");
        }

        writer.write("new ");

        if (_isArrayExpression) {
            writer.write("[] ");
        }

        if (_expressions.Nodes.size() != 0) {
            writer.write("(");
            _expressions.Print(writer);
            writer.write(")");
        }

        _typeNode.Print(writer);

        if (_initializers.Nodes.size() != 0) {
            writer.write("(");
            _initializers.Print(writer);
            writer.write(")");
        }
    }
}