package ast;

import java.io.StringWriter;

public class ConversionExpression extends BaseNode {
    private BaseNode _typeNode;
    private BaseNode _expressions;

    public ConversionExpression(BaseNode typeNode, BaseNode expressions) {
        super(NodeType.ConversionExpression);
        _typeNode = typeNode;
        _expressions = expressions;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("(");
        _typeNode.Print(writer);
        writer.write(")(");
        _expressions.Print(writer);
    }
}