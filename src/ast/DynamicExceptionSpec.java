package ast;

import java.io.StringWriter;

public class DynamicExceptionSpec extends ParentNode {
    public DynamicExceptionSpec(BaseNode child) {
        super(NodeType.DynamicExceptionSpec, child);
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("throw(");
        child.Print(writer);
        writer.write(")");
    }
}