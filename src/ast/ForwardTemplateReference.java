package ast;

import java.io.StringWriter;

public class ForwardTemplateReference extends BaseNode {
    // TODO: Compute inside the Demangler
    public BaseNode Reference;
    private int _index;

    public ForwardTemplateReference(int index) {
        super(NodeType.ForwardTemplateReference);
        _index = index;
    }

    @Override
    public String GetName() {
        return Reference.GetName();
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        Reference.PrintLeft(writer);
    }

    @Override
    public void PrintRight(StringWriter writer) {
        Reference.PrintRight(writer);
    }

    @Override
    public boolean HasRightPart() {
        return Reference.HasRightPart();
    }
}
