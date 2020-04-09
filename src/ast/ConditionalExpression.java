package ast;

import java.io.StringWriter;

public class ConditionalExpression extends BaseNode {
    private BaseNode _thenNode;
    private BaseNode _elseNode;
    private BaseNode _conditionNode;

    public ConditionalExpression(BaseNode conditionNode, BaseNode thenNode, BaseNode elseNode) {
        super(NodeType.ConditionalExpression);
        _thenNode = thenNode;
        _conditionNode = conditionNode;
        _elseNode = elseNode;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("(");
        _conditionNode.Print(writer);
        writer.write(") ? (");
        _thenNode.Print(writer);
        writer.write(") : (");
        _elseNode.Print(writer);
        writer.write(")");
    }
}