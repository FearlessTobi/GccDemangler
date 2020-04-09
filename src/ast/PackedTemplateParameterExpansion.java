package ast;

import java.io.StringWriter;

public class PackedTemplateParameterExpansion extends ParentNode {
    public PackedTemplateParameterExpansion(BaseNode child) {
        super(NodeType.PackedTemplateParameterExpansion, child);
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (child instanceof PackedTemplateParameter) {
            if (((PackedTemplateParameter) child).Nodes.size() != 0) {
                child.Print(writer);
            }
        } else {
            writer.write("...");
        }
    }
}