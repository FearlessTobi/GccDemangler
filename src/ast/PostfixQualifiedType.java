package ast;

import java.io.StringWriter;

public class PostfixQualifiedType extends ParentNode {
    private String _postfixQualifier;

    public PostfixQualifiedType(String postfixQualifier, BaseNode type) {
        super(NodeType.PostfixQualifiedType, type);
        _postfixQualifier = postfixQualifier;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        child.Print(writer);
        writer.write(_postfixQualifier);
    }
}