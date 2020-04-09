package ast;

import java.io.StringWriter;

public class QualifiedName extends BaseNode {
    private BaseNode _qualifier;
    private BaseNode _name;

    public QualifiedName(BaseNode qualifier, BaseNode name) {
        super(NodeType.QualifiedName);
        _qualifier = qualifier;
        _name = name;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        _qualifier.Print(writer);
        writer.write("::");
        _name.Print(writer);
    }
}