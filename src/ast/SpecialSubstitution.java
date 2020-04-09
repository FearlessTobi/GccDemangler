package ast;

import java.io.StringWriter;

public class SpecialSubstitution extends BaseNode {
    public enum SpecialType {
        Allocator,
        BasicString,
        String,
        IStream,
        OStream,
        IOStream
    }

    private SpecialType _specialSubstitutionKey;

    public SpecialSubstitution(SpecialType specialSubstitutionKey) {
        super(NodeType.SpecialSubstitution);
        _specialSubstitutionKey = specialSubstitutionKey;
    }

    public void SetExtended() {
        Type = NodeType.ExpandedSpecialSubstitution;
    }

    @Override
    public String GetName() {
        switch (_specialSubstitutionKey) {
            case Allocator:
                return "allocator";
            case BasicString:
                return "basic_string";
            case String:
                if (Type == NodeType.ExpandedSpecialSubstitution) {
                    return "basic_string";
                }

                return "string";
            case IStream:
                return "istream";
            case OStream:
                return "ostream";
            case IOStream:
                return "iostream";
        }

        return null;
    }

    //No override
    private String GetExtendedName() {
        switch (_specialSubstitutionKey) {
            case Allocator:
                return "std::allocator";
            case BasicString:
                return "std::basic_string";
            case String:
                return "std::basic_string<char, std::char_traits<char>, std::allocator<char> >";
            case IStream:
                return "std::basic_istream<char, std::char_traits<char> >";
            case OStream:
                return "std::basic_ostream<char, std::char_traits<char> >";
            case IOStream:
                return "std::basic_iostream<char, std::char_traits<char> >";
        }

        return null;
    }

    @Override
    public void PrintLeft(StringWriter writer) {
        if (Type == NodeType.ExpandedSpecialSubstitution) {
            writer.write(GetExtendedName());
        } else {
            writer.write("std::");
            writer.write(GetName());
        }
    }
}