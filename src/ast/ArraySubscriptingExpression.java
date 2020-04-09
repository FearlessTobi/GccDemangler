package ast;

import java.io.StringWriter;

public class ArraySubscriptingExpression extends BaseNode {
    private BaseNode _leftNode;
    private BaseNode _subscript;

    public ArraySubscriptingExpression(BaseNode leftNode, BaseNode subscript) {
        super(NodeType.ArraySubscriptingExpression);
        _leftNode = leftNode;
        _subscript = subscript;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("(");
        _leftNode.Print(writer);
        writer.write(")[");
        _subscript.Print(writer);
        writer.write("]");
    }
}