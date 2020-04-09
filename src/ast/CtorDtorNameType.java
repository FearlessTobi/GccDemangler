package ast;

import java.io.StringWriter;

public class CtorDtorNameType extends ParentNode {
    private boolean _isDestructor;

    public CtorDtorNameType(BaseNode name, boolean isDestructor) {
        super(NodeType.CtorDtorNameType, name);
        _isDestructor = isDestructor;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (_isDestructor) {
            writer.write("~");
        }

        writer.write(child.GetName());
    }
}