package ast;

import java.io.StringWriter;

public class CvType extends ParentNode {

    public class Cv {
        public static final int None = 1;
        public static final int Const = 2;
        public static final int Volatile = 3;
        public static final int Restricted = 4;
    }

    public int Qualifier;

    public CvType(int qualifier, BaseNode child) {
        super(NodeType.CvQualifierType, child);
        Qualifier = qualifier;
    }

    public void PrintQualifier(StringWriter writer) {
        if ((Qualifier & Cv.Const) != 0) {
            writer.write(" const");
        }

        if ((Qualifier & Cv.Volatile) != 0) {
            writer.write(" volatile");
        }

        if ((Qualifier & Cv.Restricted) != 0) {
            writer.write(" restrict");
        }
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (child != null) {
            child.PrintLeft(writer);
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