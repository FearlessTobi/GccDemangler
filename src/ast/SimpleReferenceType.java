package ast;

import java.io.StringWriter;

public class SimpleReferenceType extends ParentNode {
    public Reference Qualifier;

    public SimpleReferenceType(Reference qualifier, BaseNode child) {
        super(NodeType.SimpleReferenceType, child);
        Qualifier = qualifier;
    }

    public void PrintQualifier(StringWriter writer) {
       /* if ((Qualifier & Reference.LValue) != 0) {
            writer.write("&");
        }

        if ((Qualifier & Reference.RValue) != 0) {
            writer.write("&&");
        }*/
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (child != null) {
            child.PrintLeft(writer);
        } else if (Qualifier != Reference.None) {
            writer.write(" ");
        }

        PrintQualifier(writer);
    }

    @Override
    public boolean HasRightPart() {
        return child != null && child.HasRightPart();
    }

    @Override
    public void PrintRight(StringWriter writer) {
        if (child != null) {
            child.PrintRight(writer);
        }
    }
}
