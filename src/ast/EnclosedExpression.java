package ast;

import java.io.StringWriter;

public class EnclosedExpression extends BaseNode {
    private String _prefix;
    private BaseNode _expression;
    private String _postfix;

    public EnclosedExpression(String prefix, BaseNode expression, String postfix) {
        super(NodeType.EnclosedExpression);
        _prefix = prefix;
        _expression = expression;
        _postfix = postfix;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write(_prefix);
        _expression.Print(writer);
        writer.write(_postfix);
    }
}