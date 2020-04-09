package de.fearlesstobi.demangler.ast

import java.io.StringWriter

class SpecialSubstitution(private val specialSubstitutionKey: SpecialType) : BaseNode(NodeType.SpecialSubstitution) {
    enum class SpecialType {
        Allocator, BasicString, String, IStream, OStream, IOStream
    }

    fun SetExtended() {
        type = NodeType.ExpandedSpecialSubstitution
    }

    override fun getName(): String? {
        return when (specialSubstitutionKey) {
            SpecialType.Allocator -> "allocator"
            SpecialType.BasicString -> "basic_string"
            SpecialType.String -> {
                if (type == NodeType.ExpandedSpecialSubstitution) {
                    "basic_string"
                } else "string"
            }
            SpecialType.IStream -> "istream"
            SpecialType.OStream -> "ostream"
            SpecialType.IOStream -> "iostream"
        }
        return null
    }

    //No override
    private fun GetExtendedName(): String {
        return when (specialSubstitutionKey) {
            SpecialType.Allocator -> "std::allocator"
            SpecialType.BasicString -> "std::basic_string"
            SpecialType.String -> "std::basic_string<char, std::char_traits<char>, std::allocator<char> >"
            SpecialType.IStream -> "std::basic_istream<char, std::char_traits<char> >"
            SpecialType.OStream -> "std::basic_ostream<char, std::char_traits<char> >"
            SpecialType.IOStream -> "std::basic_iostream<char, std::char_traits<char> >"
        }
        return ""
    }

    public override fun printLeft(writer: StringWriter) {
        if (type == NodeType.ExpandedSpecialSubstitution) {
            writer.write(GetExtendedName())
        } else {
            writer.write("std::")
            writer.write(getName())
        }
    }

}