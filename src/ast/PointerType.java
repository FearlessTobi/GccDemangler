package ast;

import java.io.StringWriter;

public class PointerType extends BaseNode {
    private BaseNode _child;

    public PointerType(BaseNode child) {
        super(NodeType.PointerType);
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

        writer.write("*");
    }

    @Override
    public void PrintRight(StringWriter writer) {
        if (_child.IsArray() || _child.HasFunctions()) {
            writer.write(")");
        }

        _child.PrintRight(writer);
    }
}