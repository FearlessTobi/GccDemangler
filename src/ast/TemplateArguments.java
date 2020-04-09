package ast;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TemplateArguments extends NodeArray {
    public TemplateArguments(List<BaseNode> nodes) {
        super(nodes, NodeType.TemplateArguments);
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        List<String> nodeStrings = new LinkedList<>();
        for(BaseNode node: Nodes){
            nodeStrings.add(node.toString());
        }
        String[] nodeStringsArr = nodeStrings.toArray(new String[0]);

        String Params = String.join(", ", nodeStringsArr);

        writer.write("<");

        writer.write(Params);

        if (Params.endsWith(">")) {
            writer.write(" ");
        }

        writer.write(">");
    }
}