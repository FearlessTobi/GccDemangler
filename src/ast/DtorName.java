package ast;

import java.io.StringWriter;

public class DtorName extends ParentNode {
    public DtorName(BaseNode name) {
        super(NodeType.DtOrName, name);
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("~");
        child.PrintLeft(writer);
    }
}