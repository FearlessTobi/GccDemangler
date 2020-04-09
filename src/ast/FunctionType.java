package ast;

import java.io.StringWriter;

public class FunctionType extends BaseNode {
    private BaseNode _returnType;
    private BaseNode _params;
    private BaseNode _cvQualifier;
    private SimpleReferenceType _referenceQualifier;
    private BaseNode _exceptionSpec;

    public FunctionType(BaseNode returnType, BaseNode Params, BaseNode cvQualifier, SimpleReferenceType referenceQualifier, BaseNode exceptionSpec) {
        super(NodeType.FunctionType);
        _returnType = returnType;
        _params = Params;
        _cvQualifier = cvQualifier;
        _referenceQualifier = referenceQualifier;
        _exceptionSpec = exceptionSpec;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        _returnType.PrintLeft(writer);
        writer.write(" ");
    }

    @Override
    public void PrintRight(StringWriter writer) {
        writer.write("(");
        _params.Print(writer);
        writer.write(")");

        _returnType.PrintRight(writer);

        _cvQualifier.Print(writer);

        if (_referenceQualifier.Qualifier != Reference.None) {
            writer.write(" ");
            _referenceQualifier.PrintQualifier(writer);
        }

        if (_exceptionSpec != null) {
            writer.write(" ");
            _exceptionSpec.Print(writer);
        }
    }

    @Override
    public boolean HasRightPart() {
        return true;
    }

    @Override
    public boolean HasFunctions() {
        return true;
    }
}