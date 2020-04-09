package ast;

import java.io.StringWriter;

public class IntegerLiteral extends BaseNode {
    private String _literalName;
    private String _literalValue;

    public IntegerLiteral(String literalName, String literalValue) {
        super(NodeType.IntegerLiteral);
        _literalValue = literalValue;
        _literalName = literalName;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (_literalName.length() > 3) {
            writer.write("(");
            writer.write(_literalName);
            writer.write(")");
        }

        if (_literalValue.charAt(0) == 'n') {
            writer.write("-");
            writer.write(_literalValue.substring(1));
        } else {
            writer.write(_literalValue);
        }

        if (_literalName.length() <= 3) {
            writer.write(_literalName);
        }
    }
}