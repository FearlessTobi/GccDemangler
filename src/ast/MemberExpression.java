package ast;

import java.io.StringWriter;

public class MemberExpression extends BaseNode {
    private BaseNode _leftNode;
    private String _kind;
    private BaseNode _rightNode;

    public MemberExpression(BaseNode leftNode, String kind, BaseNode rightNode) {
        super(NodeType.MemberExpression);
        _leftNode = leftNode;
        _kind = kind;
        _rightNode = rightNode;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        _leftNode.Print(writer);
        writer.write(_kind);
        _rightNode.Print(writer);
    }
}