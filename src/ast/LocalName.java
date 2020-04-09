package ast;

import java.io.StringWriter;

public class LocalName extends BaseNode {
    private BaseNode _encoding;
    private BaseNode _entity;

    public LocalName(BaseNode encoding, BaseNode entity) {
        super(NodeType.LocalName);
        _encoding = encoding;
        _entity = entity;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        _encoding.Print(writer);
        writer.write("::");
        _entity.Print(writer);
    }
}