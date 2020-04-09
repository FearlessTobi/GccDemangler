package ast;

import java.io.StringWriter;

public abstract class BaseNode {
    public NodeType Type;

    public BaseNode(NodeType type) {
        Type = type;
    }

    //virtual
    public void Print(StringWriter writer) {
        PrintLeft(writer);

        if (HasRightPart()) {
            PrintRight(writer);
        }
    }

    public abstract void PrintLeft(StringWriter writer);

    //virtual
    public boolean HasRightPart() {
        return false;
    }

    //virtual
    public boolean IsArray() {
        return false;
    }

    //virtual
    public boolean HasFunctions() {
        return false;
    }

    //virtual
    public String GetName() {
        return null;
    }

    //virtual
    public void PrintRight(StringWriter writer) {
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();

        Print(writer);

        return writer.toString();
    }
}