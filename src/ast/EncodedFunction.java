package ast;

import java.io.StringWriter;

public class EncodedFunction extends BaseNode {
    private BaseNode _name;
    private BaseNode _params;
    private BaseNode _cv;
    private BaseNode _ref;
    private BaseNode _attrs;
    private BaseNode _ret;

    public EncodedFunction(BaseNode name, BaseNode Params, BaseNode cv, BaseNode Ref, BaseNode attrs, BaseNode ret) {
        super(NodeType.NameType);
        _name = name;
        _params = Params;
        _cv = cv;
        _ref = Ref;
        _attrs = attrs;
        _ret = ret;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (_ret != null) {
            _ret.PrintLeft(writer);

            if (!_ret.HasRightPart()) {
                writer.write(" ");
            }
        }

        _name.Print(writer);

    }

    @Override
    public boolean HasRightPart() {
        return true;
    }

    @Override
    public void PrintRight(StringWriter writer) {
        writer.write("(");

        if (_params != null) {
            _params.Print(writer);
        }

        writer.write(")");

        if (_ret != null) {
            _ret.PrintRight(writer);
        }

        if (_cv != null) {
            _cv.Print(writer);
        }

        if (_ref != null) {
            _ref.Print(writer);
        }

        if (_attrs != null) {
            _attrs.Print(writer);
        }
    }
}