package ast;

import java.io.StringWriter;

public class BinaryExpression extends BaseNode {
    private BaseNode _leftPart;
    private String _name;
    private BaseNode _rightPart;

    public BinaryExpression(BaseNode leftPart, String name, BaseNode rightPart) {
        super(NodeType.BinaryExpression);
        _leftPart = leftPart;
        _name = name;
        _rightPart = rightPart;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (_name.equals(">")) {
            writer.write("(");
        }

        writer.write("(");
        _leftPart.Print(writer);
        writer.write(") ");

        writer.write(_name);

        writer.write(" (");
        _rightPart.Print(writer);
        writer.write(")");

        if (_name.equals(">")) {
            writer.write(")");
        }
    }
}