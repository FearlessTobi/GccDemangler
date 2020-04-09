package ast;

import java.io.StringWriter;

public class GlobalQualifiedName extends ParentNode {
    public GlobalQualifiedName(BaseNode child) {
        super(NodeType.GlobalQualifiedName, child);
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("::");
        child.Print(writer);
    }
}
