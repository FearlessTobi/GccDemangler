package ast;

import java.io.StringWriter;

public class NameTypeWithTemplateArguments extends BaseNode {
    private BaseNode _prev;
    private BaseNode _templateArgument;

    public NameTypeWithTemplateArguments(BaseNode prev, BaseNode templateArgument) {
        super(NodeType.NameTypeWithTemplateArguments);
        _prev = prev;
        _templateArgument = templateArgument;
    }

    @Override
    public String GetName() {
        return _prev.GetName();
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        _prev.Print(writer);
        _templateArgument.Print(writer);
    }
}