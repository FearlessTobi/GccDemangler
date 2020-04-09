package ast;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

public class NodeArray extends BaseNode {
    public List<BaseNode> Nodes;

    public NodeArray(List<BaseNode> nodes) {
        super(NodeType.NodeArray);
        Nodes = nodes;
    }

    public NodeArray(List<BaseNode> nodes, NodeType type) {
        super(type);
        Nodes = nodes;
    }

    @Override
    public boolean IsArray() {
        return true;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        List<String> nodeStrings = new LinkedList<>();
        for(BaseNode node: Nodes){
            nodeStrings.add(node.toString());
        }
        String[] nodeStringsArr = nodeStrings.toArray(new String[0]);
        writer.write(String.join(", ", nodeStringsArr));
    }
}