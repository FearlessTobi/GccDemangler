package ast;

import java.io.StringWriter;

public class CastExpression extends BaseNode {
    private String _kind;
    private BaseNode _to;
    private BaseNode _from;

    public CastExpression(String kind, BaseNode to, BaseNode from) {
        super(NodeType.CastExpression);
        _kind = kind;
        _to = to;
        _from = from;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write(_kind);
        writer.write("<");
        _to.PrintLeft(writer);
        writer.write(">(");
        _from.PrintLeft(writer);
        writer.write(")");
    }
}