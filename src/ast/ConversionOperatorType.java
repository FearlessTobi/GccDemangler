package ast;

import java.io.StringWriter;

public class ConversionOperatorType extends ParentNode {
    public ConversionOperatorType(BaseNode child) {
        super(NodeType.ConversionOperatorType, child);
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("operator ");
        child.Print(writer);
    }
}