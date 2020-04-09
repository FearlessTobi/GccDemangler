package ast;

import java.io.StringWriter;

public class ReferenceType extends BaseNode {
    private String _reference;
    private BaseNode _child;

    public ReferenceType(String reference, BaseNode child) {
        super(NodeType.ReferenceType);
        _reference = reference;
        _child = child;
    }

    @Override
    public boolean HasRightPart() {
        return _child.HasRightPart();
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        _child.PrintLeft(writer);

        if (_child.IsArray()) {
            writer.write(" ");
        }

        if (_child.IsArray() || _child.HasFunctions()) {
            writer.write("(");
        }

        writer.write(_reference);
    }

    @Override
    public void PrintRight(StringWriter writer) {
        if (_child.IsArray() || _child.HasFunctions()) {
            writer.write(")");
        }

        _child.PrintRight(writer);
    }
}