package ast;

import java.io.StringWriter;

public class NoexceptSpec extends ParentNode {
    public NoexceptSpec(BaseNode child) {
        super(NodeType.NoexceptSpec, child);
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("noexcept(");
        child.Print(writer);
        writer.write(")");
    }
}
