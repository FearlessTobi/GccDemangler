package ast;

import java.io.StringWriter;

public class ArrayType extends BaseNode {
    private BaseNode _base;
    private BaseNode _dimensionExpression;
    private String _dimensionString;

    public ArrayType(BaseNode Base) {
        super(NodeType.ArrayType);
        _base = Base;
        _dimensionExpression = null;
    }

    public ArrayType(BaseNode Base, BaseNode dimensionExpression) {
        super(NodeType.ArrayType);
        _base = Base;
        _dimensionExpression = dimensionExpression;
    }

    public ArrayType(BaseNode Base, String dimensionString) {
        super(NodeType.ArrayType);
        _base = Base;
        _dimensionString = dimensionString;
    }

    @Override
    public boolean HasRightPart() {
        return true;
    }

    @Override
    public boolean IsArray() {
        return true;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        _base.PrintLeft(writer);
    }

    @Override
    public void PrintRight(StringWriter writer) {
        // FIXME: detect if previous char was a ].
        writer.write(" ");

        writer.write("[");

        if (_dimensionString != null) {
            writer.write(_dimensionString);
        } else if (_dimensionExpression != null) {
            _dimensionExpression.Print(writer);
        }

        writer.write("]");

        _base.PrintRight(writer);
    }
}