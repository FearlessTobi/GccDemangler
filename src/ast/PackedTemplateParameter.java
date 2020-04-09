package ast;

import java.io.StringWriter;
import java.util.List;

public class PackedTemplateParameter extends NodeArray {
    public PackedTemplateParameter(List<BaseNode> nodes) {
        super(nodes, NodeType.PackedTemplateParameter);
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        for (BaseNode node : Nodes) {
            node.PrintLeft(writer);
        }
    }

    @Override
    public void PrintRight(StringWriter writer) {
        for (BaseNode node : Nodes) {
            node.PrintLeft(writer);
        }
    }

    @Override
    public boolean HasRightPart() {
        for (BaseNode node : Nodes) {
            if (node.HasRightPart()) {
                return true;
            }
        }

        return false;
    }
}