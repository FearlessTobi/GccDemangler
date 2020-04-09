package ast;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

public class InitListExpression extends BaseNode {
    private BaseNode _typeNode;
    private List<BaseNode> _nodes;

    public InitListExpression(BaseNode typeNode, List<BaseNode> nodes) {
        super(NodeType.InitListExpression);
        _typeNode = typeNode;
        _nodes = nodes;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (_typeNode != null) {
            _typeNode.Print(writer);
        }

        writer.write("{");

        List<String> nodeStrings = new LinkedList<>();
        for (BaseNode node : _nodes) {
            nodeStrings.add(node.toString());
        }
        String[] nodeStringsArr = nodeStrings.toArray(new String[0]);
        writer.write(String.join(", ", nodeStringsArr));

        writer.write("}");
    }
}