package ast;

import java.io.StringWriter;

public class FunctionParameter extends BaseNode {
    private String _number;

    public FunctionParameter(String number) {
        super(NodeType.FunctionParameter);
        _number = number;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        writer.write("fp ");

        if (_number != null) {
            writer.write(_number);
        }
    }
}