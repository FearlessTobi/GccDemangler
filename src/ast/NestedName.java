package ast;

import java.io.StringWriter;

public class NestedName extends ParentNode {
    private BaseNode _name;

    public NestedName(BaseNode name, BaseNode type) {
        super(NodeType.NestedName, type);
        _name = name;
    }

    @Override
    public String GetName() {
        return _name.GetName();
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        child.Print(writer);
        writer.write("::");
        _name.Print(writer);
    }
}