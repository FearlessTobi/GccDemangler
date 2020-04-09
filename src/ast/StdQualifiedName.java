package ast;

import java.io.StringWriter;

public class StdQualifiedName extends ParentNode {
    public StdQualifiedName(BaseNode child) {
        super(NodeType.StdQualifiedName, child);
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("std::");
        child.Print(writer);
    }
}
