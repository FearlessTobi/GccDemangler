package ast;

import java.io.StringWriter;

public class NameType extends BaseNode {
    private String _nameValue;

    public NameType(String nameValue, NodeType type) {
        super(type);
        _nameValue = nameValue;
    }

    public NameType(String nameValue) {
        super(NodeType.NameType);
        _nameValue = nameValue;
    }

    @Override
    public String GetName() {
        return _nameValue;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write(_nameValue);
    }
}