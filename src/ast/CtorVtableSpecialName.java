package ast;

import java.io.StringWriter;

public class CtorVtableSpecialName extends BaseNode {
    private BaseNode _firstType;
    private BaseNode _secondType;

    public CtorVtableSpecialName(BaseNode firstType, BaseNode secondType) {
        super(NodeType.CtorVtableSpecialName);
        _firstType = firstType;
        _secondType = secondType;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("construction vtable for ");
        _firstType.Print(writer);
        writer.write("-in-");
        _secondType.Print(writer);
    }
}