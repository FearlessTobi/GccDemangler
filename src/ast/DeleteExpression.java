package ast;

import java.io.StringWriter;

public class DeleteExpression extends ParentNode {
    private boolean _isGlobal;
    private boolean _isArrayExpression;

    public DeleteExpression(BaseNode child, boolean isGlobal, boolean isArrayExpression) {
        super(NodeType.DeleteExpression, child);
        _isGlobal = isGlobal;
        _isArrayExpression = isArrayExpression;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (_isGlobal) {
            writer.write("::");
        }

        writer.write("delete");

        if (_isArrayExpression) {
            writer.write("[] ");
        }

        child.Print(writer);
    }
}