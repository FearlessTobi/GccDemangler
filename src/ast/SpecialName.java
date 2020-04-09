package ast;

import java.io.StringWriter;

public class SpecialName extends ParentNode {
    private String _specialValue;

    public SpecialName(String specialValue, BaseNode type) {
        super(NodeType.SpecialName, type);
        _specialValue = specialValue;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write(_specialValue);
        child.Print(writer);
    }
}