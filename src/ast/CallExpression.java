package ast;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

public class CallExpression extends NodeArray {
    private BaseNode _callee;

    public CallExpression(BaseNode callee, List<BaseNode> nodes) {
        super(nodes, NodeType.CallExpression);
        _callee = callee;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        _callee.Print(writer);

        writer.write("(");

        List<String> nodeStrings = new LinkedList<>();
        for (BaseNode node : Nodes) {
            nodeStrings.add(node.toString());
        }
        String[] nodeStringsArr = nodeStrings.toArray(new String[0]);

        writer.write(String.join(", ", nodeStringsArr));
        writer.write(")");
    }
}