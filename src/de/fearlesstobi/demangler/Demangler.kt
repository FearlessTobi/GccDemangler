package de.fearlesstobi.demangler

import de.fearlesstobi.demangler.ast.*
import de.fearlesstobi.demangler.ast.CvType.Cv
import java.io.StringWriter
import java.util.*

class Demangler private constructor(private val mangled: String) {
    private val substitutionList: MutableList<BaseNode?> = LinkedList()
    private var templateParamList: MutableList<BaseNode> = LinkedList()
    private var position = 0
    private val length: Int
    private var canForwardTemplateReference = false
    private var canParseTemplateArgs: Boolean
    private fun consumeIf(toConsume: String): Boolean {
        val mangledPart = mangled.substring(position)
        if (mangledPart.startsWith(toConsume)) {
            position += toConsume.length
            return true
        }
        return false
    }

    private fun peekString(offset: Int, length: Int): String? {
        return if (position + offset >= length) {
            null
        } else mangled.substring(position + offset, position + offset + length)
    }

    private fun peek(offset: Int = 0): Char {
        return if (position + offset >= length) {
            '\u0000'
        } else mangled[position + offset]
    }

    private fun consume(): Char {
        return if (position < length) {
            mangled[position++]
        } else '\u0000'
    }

    private fun count(): Int {
        return length - position
    }

    private fun parseSeqId(): Int {
        val part = mangled.substring(position)
        var seqIdLen = 0
        while (seqIdLen < part.length) {
            if (!Character.isLetterOrDigit(part[seqIdLen])) {
                break
            }
            seqIdLen++
        }
        position += seqIdLen
        return fromBase36(part.substring(0, seqIdLen))
    }

    //   <substitution> ::= S <seq-id> _
//                  ::= S_
//                  ::= St # std::
//                  ::= Sa # std::allocator
//                  ::= Sb # std::basic_String
//                  ::= Ss # std::basic_String<char, std::char_traits<char>, std::allocator<char> >
//                  ::= Si # std::basic_istream<char, std::char_traits<char> >
//                  ::= So # std::basic_ostream<char, std::char_traits<char> >
//                  ::= Sd # std::basic_iostream<char, std::char_traits<char> >
    private fun parseSubstitution(): BaseNode? {
        if (!consumeIf("S")) {
            return null
        }
        val substitutionSecondChar = peek()
        if (Character.isLowerCase(substitutionSecondChar)) {
            return when (substitutionSecondChar) {
                'a' -> {
                    position++
                    SpecialSubstitution(SpecialSubstitution.SpecialType.Allocator)
                }
                'b' -> {
                    position++
                    SpecialSubstitution(SpecialSubstitution.SpecialType.BasicString)
                }
                's' -> {
                    position++
                    SpecialSubstitution(SpecialSubstitution.SpecialType.String)
                }
                'i' -> {
                    position++
                    SpecialSubstitution(SpecialSubstitution.SpecialType.IStream)
                }
                'o' -> {
                    position++
                    SpecialSubstitution(SpecialSubstitution.SpecialType.OStream)
                }
                'd' -> {
                    position++
                    SpecialSubstitution(SpecialSubstitution.SpecialType.IOStream)
                }
                else -> null
            }
        }
        // ::= S_
        if (consumeIf("_")) {
            return if (!substitutionList.isEmpty()) {
                substitutionList[0]
            } else null
        }
        //                ::= S <seq-id> _
        var seqId = parseSeqId()
        if (seqId < 0) {
            return null
        }
        seqId++
        return if (!consumeIf("_") || seqId >= substitutionList.size) {
            null
        } else substitutionList[seqId]
    }

    // NOTE: thoses data aren't used in the output
//  <call-offset> ::= h <nv-offset> _
//                ::= v <v-offset> _
//  <nv-offset>   ::= <offset number>
//                    # non-virtual base override
//  <v-offset>    ::= <offset number> _ <virtual offset number>
//                    # virtual base override, with vcall offset
    private fun parseCallOffset(): Boolean {
        if (consumeIf("h")) {
            return parseNumber(true)!!.length == 0 || !consumeIf("_")
        } else if (consumeIf("v")) {
            return parseNumber(true)!!.length == 0 || !consumeIf("_") || parseNumber(true)!!.length == 0 || !consumeIf("_")
        }
        return true
    }

    //   <class-enum-type> ::= <name>     # non-dependent type name, dependent type name, or dependent typename-specifier
//                     ::= Ts <name>  # dependent elaborated type specifier using 'struct' or 'class'
//                     ::= Tu <name>  # dependent elaborated type specifier using 'union'
//                     ::= Te <name>  # dependent elaborated type specifier using 'enum'
    private fun parseClassEnumType(): BaseNode? {
        var elaboratedType: String? = null
        if (consumeIf("Ts")) {
            elaboratedType = "struct"
        } else if (consumeIf("Tu")) {
            elaboratedType = "union"
        } else if (consumeIf("Te")) {
            elaboratedType = "enum"
        }
        val name = parseName() ?: return null
        return elaboratedType?.let { ElaboratedType(it, name) } ?: name
    }

    //  <function-type>         ::= [<CV-qualifiers>] [<exception-spec>] [Dx] F [Y] <bare-function-type> [<ref-qualifier>] E
//  <bare-function-type>    ::= <signature type>+
//                              # types are possible return type, then parameter types
//  <exception-spec>        ::= Do                # non-throwing exception-specification (e.g., noexcept, throw())
//                          ::= DO <expression> E # computed (instantiation-dependent) noexcept
//                          ::= Dw <type>+ E      # dynamic exception specification with instantiation-dependent types
    private fun parseFunctionType(): BaseNode? {
        val cvQualifiers = parseCvQualifiers()
        var exceptionSpec: BaseNode? = null
        if (consumeIf("Do")) {
            exceptionSpec = NameType("noexcept")
        } else if (consumeIf("DO")) {
            val expression = parseExpression()
            if (expression == null || !consumeIf("E")) {
                return null
            }
            exceptionSpec = NoexceptSpec(expression)
        } else if (consumeIf("Dw")) {
            val types: MutableList<BaseNode?> = ArrayList()
            while (!consumeIf("E")) {
                val type = parseType() ?: return null
                types.add(type)
            }
            exceptionSpec = DynamicExceptionSpec(NodeArray(types))
        }
        // We don't need the transaction
        consumeIf("Dx")
        if (!consumeIf("F")) {
            return null
        }
        // extern "C"
        consumeIf("Y")
        val returnType = parseType() ?: return null
        var referenceQualifier = Reference.None
        val params: MutableList<BaseNode?> = ArrayList()
        while (true) {
            if (consumeIf("E")) {
                break
            }
            if (consumeIf("v")) {
                continue
            }
            if (consumeIf("RE")) {
                referenceQualifier = Reference.LValue
                break
            } else if (consumeIf("OE")) {
                referenceQualifier = Reference.RValue
                break
            }
            val type = parseType() ?: return null
            params.add(type)
        }
        return FunctionType(returnType, NodeArray(params), CvType(cvQualifiers, null), SimpleReferenceType(referenceQualifier, null), exceptionSpec)
    }

    //   <array-type> ::= A <positive dimension number> _ <element type>
//                ::= A [<dimension expression>] _ <element type>
    private fun parseArrayType(): BaseNode? {
        if (!consumeIf("A")) {
            return null
        }
        val elementType: BaseNode?
        if (Character.isDigit(peek())) {
            val dimension = parseNumber()
            if (dimension!!.length == 0 || !consumeIf("_")) {
                return null
            }
            elementType = parseType()
            return elementType?.let { ArrayType(it, dimension) }
        }
        if (!consumeIf("_")) {
            val dimensionExpression = parseExpression()
            if (dimensionExpression == null || !consumeIf("_")) {
                return null
            }
            elementType = parseType()
            return elementType?.let { ArrayType(it, dimensionExpression) }
        }
        elementType = parseType()
        return elementType?.let { ArrayType(it) }
    }

    // <type>  ::= <builtin-type>
//         ::= <qualified-type> (PARTIAL)
//         ::= <function-type>
//         ::= <class-enum-type>
//         ::= <array-type> (TODO)
//         ::= <pointer-to-member-type> (TODO)
//         ::= <template-param>
//         ::= <template-template-param> <template-args>
//         ::= <decltype>
//         ::= P <type>        # pointer
//         ::= R <type>        # l-value reference
//         ::= O <type>        # r-value reference (C++11)
//         ::= C <type>        # complex pair (C99)
//         ::= G <type>        # imaginary (C99)
//         ::= <substitution>  # See Compression below
    private fun parseType(context: NameparserContext? = null): BaseNode? { // Temporary context
        var context = context
        if (context == null) {
            context = NameparserContext()
        }
        var result: BaseNode?
        when (peek()) {
            'r', 'V', 'K' -> {
                var skipRest = false
                var typePos = 0
                if (peek(typePos) == 'r') {
                    typePos++
                }
                if (peek(typePos) == 'V') {
                    typePos++
                }
                if (peek(typePos) == 'K') {
                    typePos++
                }
                if (peek(typePos) == 'F' || peek(typePos) == 'D' && (peek(typePos + 1) == 'o' || peek(typePos + 1) == 'O' || peek(typePos + 1) == 'w' || peek(typePos + 1) == 'x')) {
                    result = parseFunctionType()
                    if (result != null) {
                        substitutionList.add(result)
                    }
                    return result
                }
                val cv = parseCvQualifiers()
                result = parseType(context)
                if (result == null) {
                    return null
                }
                result = CvType(cv, result)
            }
            'U' ->  // TODO: <extended-qualifier>
                return null
            'v' -> {
                position++
                return NameType("void")
            }
            'w' -> {
                position++
                return NameType("wchar_t")
            }
            'b' -> {
                position++
                return NameType("boolean")
            }
            'c' -> {
                position++
                return NameType("char")
            }
            'a' -> {
                position++
                return NameType("signed char")
            }
            'h' -> {
                position++
                return NameType("unsigned char")
            }
            's' -> {
                position++
                return NameType("short")
            }
            't' -> {
                position++
                return NameType("unsigned short")
            }
            'i' -> {
                position++
                return NameType("int")
            }
            'j' -> {
                position++
                return NameType("unsigned int")
            }
            'l' -> {
                position++
                return NameType("long")
            }
            'm' -> {
                position++
                return NameType("unsigned long")
            }
            'x' -> {
                position++
                return NameType("long long")
            }
            'y' -> {
                position++
                return NameType("unsigned long long")
            }
            'n' -> {
                position++
                return NameType("__int128")
            }
            'o' -> {
                position++
                return NameType("unsigned __int128")
            }
            'f' -> {
                position++
                return NameType("float")
            }
            'd' -> {
                position++
                return NameType("double")
            }
            'e' -> {
                position++
                return NameType("long double")
            }
            'g' -> {
                position++
                return NameType("__float128")
            }
            'z' -> {
                position++
                return NameType("...")
            }
            'u' -> {
                position++
                return parseSourceName()
            }
            'D' -> when (peek(1)) {
                'd' -> {
                    position += 2
                    return NameType("decimal64")
                }
                'e' -> {
                    position += 2
                    return NameType("decimal128")
                }
                'f' -> {
                    position += 2
                    return NameType("decimal32")
                }
                'h' -> {
                    position += 2
                    // FIXME: GNU c++flit returns this but that is not what is supposed to be returned.
                    return NameType("half")
                }
                'i' -> {
                    position += 2
                    return NameType("char32_t")
                }
                's' -> {
                    position += 2
                    return NameType("char16_t")
                }
                'a' -> {
                    position += 2
                    return NameType("decltype(auto)")
                }
                'n' -> {
                    position += 2
                    // FIXME: GNU c++flit returns this but that is not what is supposed to be returned.
                    return NameType("decltype(nullptr)")
                }
                't', 'T' -> {
                    position += 2
                    result = parseDecltype()
                }
                'o', 'O', 'w', 'x' -> result = parseFunctionType()
                else -> return null
            }
            'F' -> result = parseFunctionType()
            'A' -> return parseArrayType()
            'M' -> {
                // TODO: <pointer-to-member-type>
                position++
                return null
            }
            'T' -> {
                // might just be a class enum type
                if (peek(1) == 's' || peek(1) == 'u' || peek(1) == 'e') {
                    result = parseClassEnumType()
                    if (result != null) {
                        substitutionList.add(result)
                    }
                    return result
                }
                result = parseTemplateParam()
                if (result == null) {
                    return null
                }
                if (canParseTemplateArgs && peek() == 'I') {
                    val templateArguments = parseTemplateArguments() ?: return null
                    result = NameTypeWithTemplateArguments(result, templateArguments)
                }
            }
            'P' -> {
                position++
                result = parseType(context)
                if (result == null) {
                    return null
                }
                result = PointerType(result)
            }
            'R' -> {
                position++
                result = parseType(context)
                if (result == null) {
                    return null
                }
                result = ReferenceType("&", result)
            }
            'O' -> {
                position++
                result = parseType(context)
                if (result == null) {
                    return null
                }
                result = ReferenceType("&&", result)
            }
            'C' -> {
                position++
                result = parseType(context)
                if (result == null) {
                    return null
                }
                result = PostfixQualifiedType(" complex", result)
            }
            'G' -> {
                position++
                result = parseType(context)
                if (result == null) {
                    return null
                }
                result = PostfixQualifiedType(" imaginary", result)
            }
            'S' -> return if (peek(1) != 't') {
                val substitution = parseSubstitution() ?: return null
                if (canParseTemplateArgs && peek() == 'I') {
                    val templateArgument = parseTemplateArgument() ?: return null
                    result = NameTypeWithTemplateArguments(substitution, templateArgument)
                    if (result != null) {
                        substitutionList.add(result)
                    }
                    return result
                }
                substitution
            } else {
                result = parseClassEnumType()
                if (result != null) {
                    substitutionList.add(result)
                }
                return result
            }
            else -> result = parseClassEnumType()
        }
        if (result != null) {
            substitutionList.add(result)
        }
        return result
    }

    // <special-name> ::= TV <type> # virtual table
//                ::= TT <type> # VTT structure (construction vtable index)
//                ::= TI <type> # typeinfo structure
//                ::= TS <type> # typeinfo name (null-terminated byte String)
//                ::= Tc <call-offset> <call-offset> <base encoding>
//                ::= TW <object name> # Thread-local wrapper
//                ::= TH <object name> # Thread-local initialization
//                ::= T <call-offset> <base encoding>
//                              # base is the nominal target function of thunk
//                ::= GV <object name>	# Guard variable for one-time initialization
    private fun parseSpecialName(context: NameparserContext): BaseNode? {
        if (peek() != 'T') {
            if (consumeIf("GV")) {
                val name = parseName() ?: return null
                return SpecialName("guard variable for ", name)
            }
            return null
        }
        val node: BaseNode?
        return when (peek(1)) {
            'V' -> {
                position += 2
                node = parseType(context)
                if (node == null) {
                    null
                } else SpecialName("vtable for ", node)
            }
            'T' -> {
                position += 2
                node = parseType(context)
                if (node == null) {
                    null
                } else SpecialName("VTT for ", node)
            }
            'I' -> {
                position += 2
                node = parseType(context)
                if (node == null) {
                    null
                } else SpecialName("typeinfo for ", node)
            }
            'S' -> {
                position += 2
                node = parseType(context)
                if (node == null) {
                    null
                } else SpecialName("typeinfo name for ", node)
            }
            'c' -> {
                position += 2
                if (parseCallOffset() || parseCallOffset()) {
                    return null
                }
                node = parseEncoding()
                if (node == null) {
                    null
                } else SpecialName("covariant return thunk to ", node)
            }
            'C' -> {
                position += 2
                val firstType = parseType()
                if (firstType == null || parseNumber(true)!!.length == 0 || !consumeIf("_")) {
                    return null
                }
                val secondType = parseType()
                CtorVtableSpecialName(secondType, firstType)
            }
            'H' -> {
                position += 2
                node = parseName()
                if (node == null) {
                    null
                } else SpecialName("thread-local initialization routine for ", node)
            }
            'W' -> {
                position += 2
                node = parseName()
                if (node == null) {
                    null
                } else SpecialName("thread-local wrapper routine for ", node)
            }
            else -> {
                position++
                val isVirtual = peek() == 'v'
                if (parseCallOffset()) {
                    return null
                }
                node = parseEncoding()
                if (node == null) {
                    return null
                }
                if (isVirtual) {
                    SpecialName("virtual thunk to ", node)
                } else SpecialName("non-virtual thunk to ", node)
            }
        }
    }

    // <CV-qualifiers>      ::= [r] [V] [K] # restrict (C99), volatile, const
    private fun parseCvQualifiers(): Int {
        var qualifiers = Cv.None
        if (consumeIf("r")) {
            qualifiers = qualifiers or Cv.Restricted
        }
        if (consumeIf("V")) {
            qualifiers = qualifiers or Cv.Volatile
        }
        if (consumeIf("K")) {
            qualifiers = qualifiers or Cv.Const
        }
        return qualifiers
    }

    // <ref-qualifier>      ::= R              # & ref-qualifier
// <ref-qualifier>      ::= O              # && ref-qualifier
    private fun parseRefQualifiers(): SimpleReferenceType {
        var result = Reference.None
        if (consumeIf("O")) {
            result = Reference.RValue
        } else if (consumeIf("R")) {
            result = Reference.LValue
        }
        return SimpleReferenceType(result, null)
    }

    private fun createNameNode(prev: BaseNode?, name: BaseNode, context: NameparserContext?): BaseNode {
        var result = name
        if (prev != null) {
            result = NestedName(name, prev)
        }
        if (context != null) {
            context.finishWithTemplateArguments = false
        }
        return result
    }

    private fun parsePositiveNumber(): Int {
        val part = mangled.substring(position)
        var numberLength = 0
        while (numberLength < part.length) {
            if (!Character.isDigit(part[numberLength])) {
                break
            }
            numberLength++
        }
        position += numberLength
        return if (numberLength == 0) {
            -1
        } else part.substring(0, numberLength).toInt()
    }

    private fun parseNumber(isSigned: Boolean = false): String? {
        if (isSigned) {
            consumeIf("n")
        }
        if (count() == 0 || !Character.isDigit(mangled[position])) {
            return null
        }
        val part = mangled.substring(position)
        var numberLength = 0
        while (numberLength < part.length) {
            if (!Character.isDigit(part[numberLength])) {
                break
            }
            numberLength++
        }
        position += numberLength
        return part.substring(0, numberLength)
    }

    // <source-name> ::= <positive length number> <identifier>
    private fun parseSourceName(): BaseNode? {
        val length = parsePositiveNumber()
        if (count() < length || length <= 0) {
            return null
        }
        val name = mangled.substring(position, position + length)
        position += length
        return if (name.startsWith("_GLOBAL__N")) {
            NameType("(anonymous namespace)")
        } else NameType(name)
    }

    // <operator-name> ::= nw    # new
//                 ::= na    # new[]
//                 ::= dl    # delete
//                 ::= da    # delete[]
//                 ::= ps    # + (unary)
//                 ::= ng    # - (unary)
//                 ::= ad    # & (unary)
//                 ::= de    # * (unary)
//                 ::= co    # ~
//                 ::= pl    # +
//                 ::= mi    # -
//                 ::= ml    # *
//                 ::= dv    # /
//                 ::= rm    # %
//                 ::= an    # &
//                 ::= or    # |
//                 ::= eo    # ^
//                 ::= aS    # =
//                 ::= pL    # +=
//                 ::= mI    # -=
//                 ::= mL    # *=
//                 ::= dV    # /=
//                 ::= rM    # %=
//                 ::= aN    # &=
//                 ::= oR    # |=
//                 ::= eO    # ^=
//                 ::= ls    # <<
//                 ::= rs    # >>
//                 ::= lS    # <<=
//                 ::= rS    # >>=
//                 ::= eq    # ==
//                 ::= ne    # !=
//                 ::= lt    # <
//                 ::= gt    # >
//                 ::= le    # <=
//                 ::= ge    # >=
//                 ::= ss    # <=>
//                 ::= nt    # !
//                 ::= aa    # &&
//                 ::= oo    # ||
//                 ::= pp    # ++ (postfix in <expression> context)
//                 ::= mm    # -- (postfix in <expression> context)
//                 ::= cm    # ,
//                 ::= pm    # ->*
//                 ::= pt    # ->
//                 ::= cl    # ()
//                 ::= ix    # []
//                 ::= qu    # ?
//                 ::= cv <type>    # (cast) (TODO)
//                 ::= li <source-name>          # operator ""
//                 ::= v <digit> <source-name>    # vendor extended operator (TODO)
    private fun parseOperatorName(context: NameparserContext?): BaseNode? {
        return when (peek()) {
            'a' -> when (peek(1)) {
                'a' -> {
                    position += 2
                    NameType("operator&&")
                }
                'd', 'n' -> {
                    position += 2
                    NameType("operator&")
                }
                'N' -> {
                    position += 2
                    NameType("operator&=")
                }
                'S' -> {
                    position += 2
                    NameType("operator=")
                }
                else -> null
            }
            'c' -> when (peek(1)) {
                'l' -> {
                    position += 2
                    NameType("operator()")
                }
                'm' -> {
                    position += 2
                    NameType("operator,")
                }
                'o' -> {
                    position += 2
                    NameType("operator~")
                }
                'v' -> {
                    position += 2
                    val canparseTemplateArgsBackup = canParseTemplateArgs
                    val canForwardTemplateReferenceBackup = canForwardTemplateReference
                    canParseTemplateArgs = false
                    canForwardTemplateReference = canForwardTemplateReferenceBackup || context != null
                    val type = parseType()
                    canParseTemplateArgs = canparseTemplateArgsBackup
                    canForwardTemplateReference = canForwardTemplateReferenceBackup
                    if (type == null) {
                        return null
                    }
                    if (context != null) {
                        context.ctorDtorConversion = true
                    }
                    ConversionOperatorType(type)
                }
                else -> null
            }
            'd' -> when (peek(1)) {
                'a' -> {
                    position += 2
                    NameType("operator delete[]")
                }
                'e' -> {
                    position += 2
                    NameType("operator*")
                }
                'l' -> {
                    position += 2
                    NameType("operator delete")
                }
                'v' -> {
                    position += 2
                    NameType("operator/")
                }
                'V' -> {
                    position += 2
                    NameType("operator/=")
                }
                else -> null
            }
            'e' -> when (peek(1)) {
                'o' -> {
                    position += 2
                    NameType("operator^")
                }
                'O' -> {
                    position += 2
                    NameType("operator^=")
                }
                'q' -> {
                    position += 2
                    NameType("operator==")
                }
                else -> null
            }
            'g' -> when (peek(1)) {
                'e' -> {
                    position += 2
                    NameType("operator>=")
                }
                't' -> {
                    position += 2
                    NameType("operator>")
                }
                else -> null
            }
            'i' -> {
                if (peek(1) == 'x') {
                    position += 2
                    return NameType("operator[]")
                }
                null
            }
            'l' -> when (peek(1)) {
                'e' -> {
                    position += 2
                    NameType("operator<=")
                }
                'i' -> {
                    position += 2
                    val sourceName = parseSourceName() ?: return null
                    LiteralOperator(sourceName)
                }
                's' -> {
                    position += 2
                    NameType("operator<<")
                }
                'S' -> {
                    position += 2
                    NameType("operator<<=")
                }
                't' -> {
                    position += 2
                    NameType("operator<")
                }
                else -> null
            }
            'm' -> when (peek(1)) {
                'i' -> {
                    position += 2
                    NameType("operator-")
                }
                'I' -> {
                    position += 2
                    NameType("operator-=")
                }
                'l' -> {
                    position += 2
                    NameType("operator*")
                }
                'L' -> {
                    position += 2
                    NameType("operator*=")
                }
                'm' -> {
                    position += 2
                    NameType("operator--")
                }
                else -> null
            }
            'n' -> when (peek(1)) {
                'a' -> {
                    position += 2
                    NameType("operator new[]")
                }
                'e' -> {
                    position += 2
                    NameType("operator!=")
                }
                'g' -> {
                    position += 2
                    NameType("operator-")
                }
                't' -> {
                    position += 2
                    NameType("operator!")
                }
                'w' -> {
                    position += 2
                    NameType("operator new")
                }
                else -> null
            }
            'o' -> when (peek(1)) {
                'o' -> {
                    position += 2
                    NameType("operator||")
                }
                'r' -> {
                    position += 2
                    NameType("operator|")
                }
                'R' -> {
                    position += 2
                    NameType("operator|=")
                }
                else -> null
            }
            'p' -> when (peek(1)) {
                'm' -> {
                    position += 2
                    NameType("operator->*")
                }
                's', 'l' -> {
                    position += 2
                    NameType("operator+")
                }
                'L' -> {
                    position += 2
                    NameType("operator+=")
                }
                'p' -> {
                    position += 2
                    NameType("operator++")
                }
                't' -> {
                    position += 2
                    NameType("operator->")
                }
                else -> null
            }
            'q' -> {
                if (peek(1) == 'u') {
                    position += 2
                    return NameType("operator?")
                }
                null
            }
            'r' -> when (peek(1)) {
                'm' -> {
                    position += 2
                    NameType("operator%")
                }
                'M' -> {
                    position += 2
                    NameType("operator%=")
                }
                's' -> {
                    position += 2
                    NameType("operator>>")
                }
                'S' -> {
                    position += 2
                    NameType("operator>>=")
                }
                else -> null
            }
            's' -> {
                if (peek(1) == 's') {
                    position += 2
                    return NameType("operator<=>")
                }
                null
            }
            'v' ->  // TODO: ::= v <digit> <source-name>    # vendor extended operator
                null
            else -> null
        }
    }

    // <unqualified-name> ::= <operator-name> [<abi-tags> (TODO)]
//                    ::= <ctor-dtor-name> (TODO)
//                    ::= <source-name>
//                    ::= <unnamed-type-name> (TODO)
//                    ::= DC <source-name>+ E      # structured binding declaration (TODO)
    private fun parseUnqualifiedName(context: NameparserContext?): BaseNode? {
        var result: BaseNode? = null
        val c = peek()
        if (c == 'U') { // TODO: Unnamed type Name
// throw new Exception("Unnamed type Name not implemented");
        } else if (Character.isDigit(c)) {
            result = parseSourceName()
        } else if (consumeIf("DC")) { // TODO: Structured Binding Declaration
// throw new Exception("Structured Binding Declaration not implemented");
        } else {
            result = parseOperatorName(context)
        }
        if (result != null) { // TODO: ABI Tags
// throw new Exception("ABI Tags not implemented");
        }
        return result
    }

    // <ctor-dtor-name> ::= C1  # complete object constructor
//                  ::= C2  # base object constructor
//                  ::= C3  # complete object allocating constructor
//                  ::= D0  # deleting destructor
//                  ::= D1  # complete object destructor
//                  ::= D2  # base object destructor
    private fun parseCtorDtorName(context: NameparserContext?, prev: BaseNode): BaseNode? {
        if (prev.type == NodeType.SpecialSubstitution && prev is SpecialSubstitution) {
            prev.SetExtended()
        }
        if (consumeIf("C")) {
            val isInherited = consumeIf("I")
            val ctorDtorType = peek()
            if (ctorDtorType != '1' && ctorDtorType != '2' && ctorDtorType != '3') {
                return null
            }
            position++
            if (context != null) {
                context.ctorDtorConversion = true
            }
            return if (isInherited && parseName(context) == null) {
                null
            } else CtorDtorNameType(prev, false)
        }
        if (consumeIf("D")) {
            val c = peek()
            if (c != '0' && c != '1' && c != '2') {
                return null
            }
            position++
            if (context != null) {
                context.ctorDtorConversion = true
            }
            return CtorDtorNameType(prev, true)
        }
        return null
    }

    // <function-param> ::= fp <top-level CV-qualifiers> _                                                                                           # L == 0, first parameter
//                  ::= fp <top-level CV-qualifiers> <parameter-2 non-negative number> _                                                         # L == 0, second and later parameters
//                  ::= fL <L-1 non-negative number> p <top-level CV-qualifiers> _                                                               # L > 0, first parameter
//                  ::= fL <L-1 non-negative number> p <top-level CV-qualifiers> <parameter-2 non-negative number> _                             # L > 0, second and later parameters
    private fun parseFunctionParameter(): BaseNode? {
        if (consumeIf("fp")) { // ignored
            parseCvQualifiers()
            return if (!consumeIf("_")) {
                null
            } else FunctionParameter(parseNumber())
        } else if (consumeIf("fL")) {
            val l1Number = parseNumber()
            if (l1Number == null || l1Number.length == 0) {
                return null
            }
            if (!consumeIf("p")) {
                return null
            }
            // ignored
            parseCvQualifiers()
            return if (!consumeIf("_")) {
                null
            } else FunctionParameter(parseNumber())
        }
        return null
    }

    // <fold-expr> ::= fL <binary-operator-name> <expression> <expression>
//             ::= fR <binary-operator-name> <expression> <expression>
//             ::= fl <binary-operator-name> <expression>
//             ::= fr <binary-operator-name> <expression>
    private fun parseFoldExpression(): BaseNode? {
        if (!consumeIf("f")) {
            return null
        }
        val foldKind = peek()
        val hasInitializer = foldKind == 'L' || foldKind == 'R'
        val isLeftFold = foldKind == 'l' || foldKind == 'L'
        if (!isLeftFold && !(foldKind == 'r' || foldKind == 'R')) {
            return null
        }
        position++
        val operatorName: String
        operatorName = when (peekString(0, 2)) {
            "aa" -> "&&"
            "an" -> "&"
            "aN" -> "&="
            "aS" -> "="
            "cm" -> ","
            "ds" -> ".*"
            "dv" -> "/"
            "dV" -> "/="
            "eo" -> "^"
            "eO" -> "^="
            "eq" -> "=="
            "ge" -> ">="
            "gt" -> ">"
            "le" -> "<="
            "ls" -> "<<"
            "lS" -> "<<="
            "lt" -> "<"
            "mi" -> "-"
            "mI" -> "-="
            "ml" -> "*"
            "mL" -> "*="
            "ne" -> "!="
            "oo" -> "||"
            "or" -> "|"
            "oR" -> "|="
            "pl" -> "+"
            "pL" -> "+="
            "rm" -> "%"
            "rM" -> "%="
            "rs" -> ">>"
            "rS" -> ">>="
            else -> return null
        }
        position += 2
        var expression: BaseNode? = parseExpression() ?: return null
        var initializer: BaseNode? = null
        if (hasInitializer) {
            initializer = parseExpression()
            if (initializer == null) {
                return null
            }
        }
        if (isLeftFold && initializer != null) {
            val temp: BaseNode? = expression
            expression = initializer
            initializer = temp
        }
        return FoldExpression(isLeftFold, operatorName, PackedTemplateParameterExpansion(expression), initializer)
    }

    //                ::= cv <type> <expression>                               # type (expression), conversion with one argument
//                ::= cv <type> _ <expression>* E                          # type (expr-list), conversion with other than one argument
    private fun parseConversionExpression(): BaseNode? {
        if (!consumeIf("cv")) {
            return null
        }
        val canparseTemplateArgsBackup = canParseTemplateArgs
        canParseTemplateArgs = false
        val type = parseType()
        canParseTemplateArgs = canparseTemplateArgsBackup
        if (type == null) {
            return null
        }
        val expressions: MutableList<BaseNode?> = ArrayList()
        if (consumeIf("_")) {
            while (!consumeIf("E")) {
                val expression = parseExpression() ?: return null
                expressions.add(expression)
            }
        } else {
            val expression = parseExpression() ?: return null
            expressions.add(expression)
        }
        return ConversionExpression(type, NodeArray(expressions))
    }

    private fun parseBinaryExpression(name: String): BaseNode? {
        val leftPart = parseExpression() ?: return null
        val rightPart = parseExpression() ?: return null
        return BinaryExpression(leftPart, name, rightPart)
    }

    private fun parsePrefixExpression(name: String): BaseNode? {
        val expression = parseExpression() ?: return null
        return PrefixExpression(name, expression)
    }

    // <braced-expression> ::= <expression>
//                     ::= di <field source-name> <braced-expression>    # .name = expr
//                     ::= dx <index expression> <braced-expression>     # [expr] = expr
//                     ::= dX <range begin expression> <range end expression> <braced-expression>
//                                                                       # [expr ... expr] = expr
    private fun parseBracedExpression(): BaseNode? {
        if (peek() == 'd') {
            val bracedExpressionNode: BaseNode?
            when (peek(1)) {
                'i' -> {
                    position += 2
                    val field = parseSourceName() ?: return null
                    bracedExpressionNode = parseBracedExpression()
                    return if (bracedExpressionNode == null) {
                        null
                    } else BracedExpression(field, bracedExpressionNode, false)
                }
                'x' -> {
                    position += 2
                    val index = parseExpression() ?: return null
                    bracedExpressionNode = parseBracedExpression()
                    return if (bracedExpressionNode == null) {
                        null
                    } else BracedExpression(index, bracedExpressionNode, true)
                }
                'X' -> {
                    position += 2
                    val rangeBeginExpression = parseExpression() ?: return null
                    val rangeEndExpression = parseExpression() ?: return null
                    bracedExpressionNode = parseBracedExpression()
                    return bracedExpressionNode?.let { BracedRangeExpression(rangeBeginExpression, rangeEndExpression, it) }
                }
            }
        }
        return parseExpression()
    }

    //               ::= [gs] nw <expression>* _ <type> E                    # new (expr-list) type
//               ::= [gs] nw <expression>* _ <type> <initializer>        # new (expr-list) type (init)
//               ::= [gs] na <expression>* _ <type> E                    # new[] (expr-list) type
//               ::= [gs] na <expression>* _ <type> <initializer>        # new[] (expr-list) type (init)
//
// <initializer> ::= pi <expression>* E                                  # parenthesized initialization
    private fun parseNewExpression(): BaseNode? {
        val isGlobal = consumeIf("gs")
        val isArray = peek(1) == 'a'
        if (!consumeIf("nw") || !consumeIf("na")) {
            return null
        }
        val expressions: MutableList<BaseNode?> = ArrayList()
        val initializers: MutableList<BaseNode?> = ArrayList()
        while (!consumeIf("_")) {
            val expression = parseExpression() ?: return null
            expressions.add(expression)
        }
        val typeNode = parseType() ?: return null
        if (consumeIf("pi")) {
            while (!consumeIf("E")) {
                val initializer = parseExpression() ?: return null
                initializers.add(initializer)
            }
        } else if (!consumeIf("E")) {
            return null
        }
        return NewExpression(NodeArray(expressions), typeNode, NodeArray(initializers), isGlobal, isArray)
    }

    // <expression> ::= <unary operator-name> <expression>
//              ::= <binary operator-name> <expression> <expression>
//              ::= <ternary operator-name> <expression> <expression> <expression>
//              ::= pp_ <expression>                                     # prefix ++
//              ::= mm_ <expression>                                     # prefix --
//              ::= cl <expression>+ E                                   # expression (expr-list), call
//              ::= cv <type> <expression>                               # type (expression), conversion with one argument
//              ::= cv <type> _ <expression>* E                          # type (expr-list), conversion with other than one argument
//              ::= tl <type> <braced-expression>* E                     # type {expr-list}, conversion with braced-init-list argument
//              ::= il <braced-expression>* E                            # {expr-list}, braced-init-list in any other context
//              ::= [gs] nw <expression>* _ <type> E                     # new (expr-list) type
//              ::= [gs] nw <expression>* _ <type> <initializer>         # new (expr-list) type (init)
//              ::= [gs] na <expression>* _ <type> E                     # new[] (expr-list) type
//              ::= [gs] na <expression>* _ <type> <initializer>         # new[] (expr-list) type (init)
//              ::= [gs] dl <expression>                                 # delete expression
//              ::= [gs] da <expression>                                 # delete[] expression
//              ::= dc <type> <expression>                               # dynamic_cast<type> (expression)
//              ::= sc <type> <expression>                               # static_cast<type> (expression)
//              ::= cc <type> <expression>                               # const_cast<type> (expression)
//              ::= rc <type> <expression>                               # reinterpret_cast<type> (expression)
//              ::= ti <type>                                            # typeid (type)
//              ::= te <expression>                                      # typeid (expression)
//              ::= st <type>                                            # sizeof (type)
//              ::= sz <expression>                                      # sizeof (expression)
//              ::= at <type>                                            # alignof (type)
//              ::= az <expression>                                      # alignof (expression)
//              ::= nx <expression>                                      # noexcept (expression)
//              ::= <template-param>
//              ::= <function-param>
//              ::= dt <expression> <unresolved-name>                    # expr.name
//              ::= pt <expression> <unresolved-name>                    # expr->name
//              ::= ds <expression> <expression>                         # expr.*expr
//              ::= sZ <template-param>                                  # sizeof...(T), size of a template parameter pack
//              ::= sZ <function-param>                                  # sizeof...(parameter), size of a function parameter pack
//              ::= sP <template-arg>* E                                 # sizeof...(T), size of a captured template parameter pack from an alias template
//              ::= sp <expression>                                      # expression..., pack expansion
//              ::= tw <expression>                                      # throw expression
//              ::= tr                                                   # throw with no operand (rethrow)
//              ::= <unresolved-name>                                    # f(p), N::f(p), ::f(p),
//                                                                       # freestanding dependent name (e.g., T::x),
//                                                                       # objectless nonstatic member reference
//              ::= <expr-primary>
    private fun parseExpression(): BaseNode? {
        val isGlobal = consumeIf("gs")
        var expression: BaseNode?
        if (count() < 2) {
            return null
        }
        val leftNode: BaseNode?
        val rightNode: BaseNode?
        when (peek()) {
            'L' -> return parseExpressionPrimary()
            'T' -> return parseTemplateParam()
            'f' -> {
                val c = peek(1)
                return if (c == 'p' || c == 'L' && Character.isDigit(peek(2))) {
                    parseFunctionParameter()
                } else parseFoldExpression()
            }
            'a' -> {
                when (peek(1)) {
                    'a' -> {
                        position += 2
                        return parseBinaryExpression("&&")
                    }
                    'd', 'n' -> {
                        position += 2
                        return parseBinaryExpression("&")
                    }
                    'N' -> {
                        position += 2
                        return parseBinaryExpression("&=")
                    }
                    'S' -> {
                        position += 2
                        return parseBinaryExpression("=")
                    }
                    't' -> {
                        position += 2
                        val type = parseType() ?: return null
                        return EnclosedExpression("alignof (", type, ")")
                    }
                    'z' -> {
                        position += 2
                        expression = parseExpression()
                        return if (expression == null) {
                            null
                        } else EnclosedExpression("alignof (", expression, ")")
                    }
                }
                return null
            }
            'c' -> {
                when (peek(1)) {
                    'c' -> {
                        position += 2
                        val to = parseType() ?: return null
                        val from = parseExpression() ?: return null
                        return CastExpression("const_cast", to, from)
                    }
                    'l' -> {
                        position += 2
                        val callee = parseExpression() ?: return null
                        val names: MutableList<BaseNode?> = ArrayList()
                        while (!consumeIf("E")) {
                            expression = parseExpression()
                            if (expression == null) {
                                return null
                            }
                            names.add(expression)
                        }
                        return CallExpression(callee, names)
                    }
                    'm' -> {
                        position += 2
                        return parseBinaryExpression(",")
                    }
                    'o' -> {
                        position += 2
                        return parsePrefixExpression("~")
                    }
                    'v' -> return parseConversionExpression()
                }
                return null
            }
            'd' -> {
                when (peek(1)) {
                    'a' -> {
                        position += 2
                        expression = parseExpression()
                        return if (expression == null) {
                            null
                        } else DeleteExpression(expression, isGlobal, true)
                    }
                    'c' -> {
                        position += 2
                        val type = parseType() ?: return null
                        expression = parseExpression()
                        return if (expression == null) {
                            null
                        } else CastExpression("dynamic_cast", type, expression)
                    }
                    'e' -> {
                        position += 2
                        return parsePrefixExpression("*")
                    }
                    'l' -> {
                        position += 2
                        expression = parseExpression()
                        return if (expression == null) {
                            null
                        } else DeleteExpression(expression, isGlobal, false)
                    }
                    'n' -> return parseUnresolvedName()
                    's' -> {
                        position += 2
                        leftNode = parseExpression()
                        if (leftNode == null) {
                            return null
                        }
                        rightNode = parseExpression()
                        return if (rightNode == null) {
                            null
                        } else MemberExpression(leftNode, ".*", rightNode)
                    }
                    't' -> {
                        position += 2
                        leftNode = parseExpression()
                        if (leftNode == null) {
                            return null
                        }
                        rightNode = parseExpression()
                        return if (rightNode == null) {
                            null
                        } else MemberExpression(leftNode, ".", rightNode)
                    }
                    'v' -> {
                        position += 2
                        return parseBinaryExpression("/")
                    }
                    'V' -> {
                        position += 2
                        return parseBinaryExpression("/=")
                    }
                }
                return null
            }
            'e' -> {
                when (peek(1)) {
                    'o' -> {
                        position += 2
                        return parseBinaryExpression("^")
                    }
                    'O' -> {
                        position += 2
                        return parseBinaryExpression("^=")
                    }
                    'q' -> {
                        position += 2
                        return parseBinaryExpression("==")
                    }
                }
                return null
            }
            'g' -> {
                when (peek(1)) {
                    'e' -> {
                        position += 2
                        return parseBinaryExpression(">=")
                    }
                    't' -> {
                        position += 2
                        return parseBinaryExpression(">")
                    }
                }
                return null
            }
            'i' -> {
                when (peek(1)) {
                    'x' -> {
                        position += 2
                        val baseNode = parseExpression() ?: return null
                        val subscript = parseExpression()
                        return ArraySubscriptingExpression(baseNode, subscript)
                    }
                    'l' -> {
                        position += 2
                        val bracedExpressions: MutableList<BaseNode?> = ArrayList()
                        while (!consumeIf("E")) {
                            expression = parseBracedExpression()
                            if (expression == null) {
                                return null
                            }
                            bracedExpressions.add(expression)
                        }
                        return InitListExpression(null, bracedExpressions)
                    }
                }
                return null
            }
            'l' -> {
                when (peek(1)) {
                    'e' -> {
                        position += 2
                        return parseBinaryExpression("<=")
                    }
                    's' -> {
                        position += 2
                        return parseBinaryExpression("<<")
                    }
                    'S' -> {
                        position += 2
                        return parseBinaryExpression("<<=")
                    }
                    't' -> {
                        position += 2
                        return parseBinaryExpression("<")
                    }
                }
                return null
            }
            'm' -> {
                when (peek(1)) {
                    'i' -> {
                        position += 2
                        return parseBinaryExpression("-")
                    }
                    'I' -> {
                        position += 2
                        return parseBinaryExpression("-=")
                    }
                    'l' -> {
                        position += 2
                        return parseBinaryExpression("*")
                    }
                    'L' -> {
                        position += 2
                        return parseBinaryExpression("*=")
                    }
                    'm' -> {
                        position += 2
                        if (consumeIf("_")) {
                            return parsePrefixExpression("--")
                        }
                        expression = parseExpression()
                        return if (expression == null) {
                            null
                        } else PostfixExpression(expression, "--")
                    }
                }
                return null
            }
            'n' -> {
                when (peek(1)) {
                    'a', 'w' -> {
                        position += 2
                        return parseNewExpression()
                    }
                    'e' -> {
                        position += 2
                        return parseBinaryExpression("!=")
                    }
                    'g' -> {
                        position += 2
                        return parsePrefixExpression("-")
                    }
                    't' -> {
                        position += 2
                        return parsePrefixExpression("!")
                    }
                    'x' -> {
                        position += 2
                        expression = parseExpression()
                        return if (expression == null) {
                            null
                        } else EnclosedExpression("noexcept (", expression, ")")
                    }
                }
                return null
            }
            'o' -> {
                when (peek(1)) {
                    'n' -> return parseUnresolvedName()
                    'o' -> {
                        position += 2
                        return parseBinaryExpression("||")
                    }
                    'r' -> {
                        position += 2
                        return parseBinaryExpression("|")
                    }
                    'R' -> {
                        position += 2
                        return parseBinaryExpression("|=")
                    }
                }
                return null
            }
            'p' -> {
                when (peek(1)) {
                    'm' -> {
                        position += 2
                        return parseBinaryExpression("->*")
                    }
                    'l', 's' -> {
                        position += 2
                        return parseBinaryExpression("+")
                    }
                    'L' -> {
                        position += 2
                        return parseBinaryExpression("+=")
                    }
                    'p' -> {
                        position += 2
                        if (consumeIf("_")) {
                            return parsePrefixExpression("++")
                        }
                        expression = parseExpression()
                        return if (expression == null) {
                            null
                        } else PostfixExpression(expression, "++")
                    }
                    't' -> {
                        position += 2
                        leftNode = parseExpression()
                        if (leftNode == null) {
                            return null
                        }
                        rightNode = parseExpression()
                        return if (rightNode == null) {
                            null
                        } else MemberExpression(leftNode, "->", rightNode)
                    }
                }
                return null
            }
            'q' -> {
                if (peek(1) == 'u') {
                    position += 2
                    val condition = parseExpression() ?: return null
                    leftNode = parseExpression()
                    if (leftNode == null) {
                        return null
                    }
                    rightNode = parseExpression()
                    return if (rightNode == null) {
                        null
                    } else ConditionalExpression(condition, leftNode, rightNode)
                }
                return null
            }
            'r' -> {
                when (peek(1)) {
                    'c' -> {
                        position += 2
                        val to = parseType() ?: return null
                        val from = parseExpression() ?: return null
                        return CastExpression("reinterpret_cast", to, from)
                    }
                    'm', 'M' -> {
                        position += 2
                        return parseBinaryExpression("%")
                    }
                    's' -> {
                        position += 2
                        return parseBinaryExpression(">>")
                    }
                    'S' -> {
                        position += 2
                        return parseBinaryExpression(">>=")
                    }
                }
                return null
            }
            's' -> {
                when (peek(1)) {
                    'c' -> {
                        position += 2
                        val to = parseType() ?: return null
                        val from = parseExpression() ?: return null
                        return CastExpression("static_cast", to, from)
                    }
                    'p' -> {
                        position += 2
                        expression = parseExpression()
                        return expression?.let { PackedTemplateParameterExpansion(it) }
                    }
                    'r' -> return parseUnresolvedName()
                    't' -> {
                        position += 2
                        val enclosedType = parseType() ?: return null
                        return EnclosedExpression("sizeof (", enclosedType, ")")
                    }
                    'z' -> {
                        position += 2
                        expression = parseExpression()
                        return if (expression == null) {
                            null
                        } else EnclosedExpression("sizeof (", expression, ")")
                    }
                    'Z' -> {
                        position += 2
                        val sizeofParamNode: BaseNode?
                        when (peek()) {
                            'T' -> {
                                // FIXME: ??? Not entire sure if it's right
                                sizeofParamNode = parseFunctionParameter()
                                return if (sizeofParamNode == null) {
                                    null
                                } else EnclosedExpression("sizeof...(", PackedTemplateParameterExpansion(sizeofParamNode), ")")
                            }
                            'f' -> {
                                sizeofParamNode = parseFunctionParameter()
                                return if (sizeofParamNode == null) {
                                    null
                                } else EnclosedExpression("sizeof...(", sizeofParamNode, ")")
                            }
                        }
                        return null
                    }
                    'P' -> {
                        position += 2
                        val arguments: MutableList<BaseNode?> = ArrayList()
                        while (!consumeIf("E")) {
                            val argument = parseTemplateArgument() ?: return null
                            arguments.add(argument)
                        }
                        return EnclosedExpression("sizeof...(", NodeArray(arguments), ")")
                    }
                }
                return null
            }
            't' -> {
                when (peek(1)) {
                    'e' -> {
                        expression = parseExpression()
                        return if (expression == null) {
                            null
                        } else EnclosedExpression("typeid (", expression, ")")
                    }
                    't' -> {
                        val enclosedType = parseExpression() ?: return null
                        return EnclosedExpression("typeid (", enclosedType, ")")
                    }
                    'l' -> {
                        position += 2
                        val typeNode = parseType() ?: return null
                        val bracedExpressions: MutableList<BaseNode?> = ArrayList()
                        while (!consumeIf("E")) {
                            expression = parseBracedExpression()
                            if (expression == null) {
                                return null
                            }
                            bracedExpressions.add(expression)
                        }
                        return InitListExpression(typeNode, bracedExpressions)
                    }
                    'r' -> {
                        position += 2
                        return NameType("throw")
                    }
                    'w' -> {
                        position += 2
                        expression = parseExpression()
                        return expression?.let { ThrowExpression(it) }
                    }
                }
                return null
            }
        }
        return if (Character.isDigit(peek())) {
            parseUnresolvedName()
        } else null
    }

    private fun parseIntegerLiteral(literalName: String): BaseNode? {
        val number = parseNumber(true)
        return if (number == null || number.length == 0 || !consumeIf("E")) {
            null
        } else IntegerLiteral(literalName, number)
    }

    // <expr-primary> ::= L <type> <value number> E                          # integer literal
//                ::= L <type> <value float> E                           # floating literal (TODO)
//                ::= L <String type> E                                  # String literal
//                ::= L <nullptr type> E                                 # nullptr literal (i.e., "LDnE")
//                ::= L <pointer type> 0 E                               # null pointer template argument
//                ::= L <type> <real-part float> _ <imag-part float> E   # complex floating point literal (C 2000)
//                ::= L _Z <encoding> E                                  # external name
    private fun parseExpressionPrimary(): BaseNode? {
        return if (!consumeIf("L")) {
            null
        } else when (peek()) {
            'w' -> {
                position++
                parseIntegerLiteral("wchar_t")
            }
            'b' -> {
                if (consumeIf("b0E")) {
                    return NameType("false", NodeType.BooleanExpression)
                }
                if (consumeIf("b1E")) {
                    NameType("true", NodeType.BooleanExpression)
                } else null
            }
            'c' -> {
                position++
                parseIntegerLiteral("char")
            }
            'a' -> {
                position++
                parseIntegerLiteral("signed char")
            }
            'h' -> {
                position++
                parseIntegerLiteral("unsigned char")
            }
            's' -> {
                position++
                parseIntegerLiteral("short")
            }
            't' -> {
                position++
                parseIntegerLiteral("unsigned short")
            }
            'i' -> {
                position++
                parseIntegerLiteral("")
            }
            'j' -> {
                position++
                parseIntegerLiteral("u")
            }
            'l' -> {
                position++
                parseIntegerLiteral("l")
            }
            'm' -> {
                position++
                parseIntegerLiteral("ul")
            }
            'x' -> {
                position++
                parseIntegerLiteral("ll")
            }
            'y' -> {
                position++
                parseIntegerLiteral("ull")
            }
            'n' -> {
                position++
                parseIntegerLiteral("__int128")
            }
            'o' -> {
                position++
                parseIntegerLiteral("unsigned __int128")
            }
            'd', 'e', 'f' ->  // TODO: floating literal
                null
            '_' -> {
                if (consumeIf("_Z")) {
                    val encoding = parseEncoding()
                    if (encoding != null && consumeIf("E")) {
                        return encoding
                    }
                }
                null
            }
            'T' -> null
            else -> {
                val type = parseType() ?: return null
                val number = parseNumber()
                if (number == null || number.length == 0 || !consumeIf("E")) {
                    null
                } else IntegerCastExpression(type, number)
            }
        }
    }

    // <decltype>  ::= Dt <expression> E  # decltype of an id-expression or class member access (C++0x)
//             ::= DT <expression> E  # decltype of an expression (C++0x)
    private fun parseDecltype(): BaseNode? {
        if (!consumeIf("D") || !consumeIf("t") && !consumeIf("T")) {
            return null
        }
        val expression = parseExpression() ?: return null
        return if (!consumeIf("E")) {
            null
        } else EnclosedExpression("decltype(", expression, ")")
    }

    // <template-param>          ::= T_ # first template parameter
//                           ::= T <parameter-2 non-negative number> _
// <template-template-param> ::= <template-param>
//                           ::= <substitution>
    private fun parseTemplateParam(): BaseNode? {
        if (!consumeIf("T")) {
            return null
        }
        var index = 0
        if (!consumeIf("_")) {
            index = parsePositiveNumber()
            if (index < 0) {
                return null
            }
            index++
            if (!consumeIf("_")) {
                return null
            }
        }
        // 5.1.8: TODO: lambda?
// if (IsParsingLambdaParameters)
//    return new de.fearlesstobi.demangler.ast.NameType("auto");
        if (canForwardTemplateReference) {
            return ForwardTemplateReference()
        }
        return if (index >= templateParamList.size) {
            null
        } else templateParamList[index]
    }

    // <template-args> ::= I <template-arg>+ E
// <template-args> ::= I <template-arg>+ E
    private fun parseTemplateArguments(hasContext: Boolean = false): BaseNode? {
        if (!consumeIf("I")) {
            return null
        }
        if (hasContext) {
            templateParamList.clear()
        }
        val args: MutableList<BaseNode?> = ArrayList()
        while (!consumeIf("E")) {
            if (hasContext) {
                val templateParamListTemp: MutableList<BaseNode> = ArrayList(templateParamList)
                var templateArgument = parseTemplateArgument()
                templateParamList = templateParamListTemp
                if (templateArgument == null) {
                    return null
                }
                args.add(templateArgument)
                if (templateArgument.type == NodeType.PackedTemplateArgument) {
                    templateArgument = PackedTemplateParameter((templateArgument as NodeArray).nodes)
                }
                templateParamList.add(templateArgument)
            } else {
                val templateArgument = parseTemplateArgument() ?: return null
                args.add(templateArgument)
            }
        }
        return TemplateArguments(args)
    }

    // <template-arg> ::= <type>                                             # type or template
//                ::= X <expression> E                                   # expression
//                ::= <expr-primary>                                     # simple expressions
//                ::= J <template-arg>* E                                # argument pack
    private fun parseTemplateArgument(): BaseNode? {
        return when (peek()) {
            'X' -> {
                position++
                val expression = parseExpression()
                if (expression == null || !consumeIf("E")) {
                    null
                } else expression
            }
            'L' -> parseExpressionPrimary()
            'J' -> {
                position++
                val templateArguments: MutableList<BaseNode?> = ArrayList()
                while (!consumeIf("E")) {
                    val templateArgument = parseTemplateArgument() ?: return null
                    templateArguments.add(templateArgument)
                }
                NodeArray(templateArguments, NodeType.PackedTemplateArgument)
            }
            else -> parseType()
        }
    }

    internal inner class NameparserContext {
        var cvType: CvType? = null
        var ref: SimpleReferenceType? = null
        var finishWithTemplateArguments = false
        var ctorDtorConversion = false
    }

    //   <unresolved-type> ::= <template-param> [ <template-args> ]            # T:: or T<X,Y>::
//                     ::= <decltype>                                      # decltype(p)::
//                     ::= <substitution>
    private fun parseUnresolvedType(): BaseNode? {
        if (peek() == 'T') {
            val templateParam = parseTemplateParam() ?: return null
            substitutionList.add(templateParam)
            return templateParam
        } else if (peek() == 'D') {
            val declType = parseDecltype() ?: return null
            substitutionList.add(declType)
            return declType
        }
        return parseSubstitution()
    }

    // <simple-id> ::= <source-name> [ <template-args> ]
    private fun parseSimpleId(): BaseNode? {
        val sourceName = parseSourceName() ?: return null
        if (peek() == 'I') {
            val templateArguments = parseTemplateArguments() ?: return null
            return NameTypeWithTemplateArguments(sourceName, templateArguments)
        }
        return sourceName
    }

    //  <destructor-name> ::= <unresolved-type>                               # e.g., ~T or ~decltype(f())
//                    ::= <simple-id>                                     # e.g., ~A<2*N>
    private fun parseDestructorName(): BaseNode? {
        val node: BaseNode?
        node = if (Character.isDigit(peek())) {
            parseSimpleId()
        } else {
            parseUnresolvedType()
        }
        return node?.let { DtorName(it) }
    }

    //  <base-unresolved-name> ::= <simple-id>                                # unresolved name
//  extension              ::= <operator-name>                            # unresolved operator-function-id
//  extension              ::= <operator-name> <template-args>            # unresolved operator template-id
//                         ::= on <operator-name>                         # unresolved operator-function-id
//                         ::= on <operator-name> <template-args>         # unresolved operator template-id
//                         ::= dn <destructor-name>                       # destructor or pseudo-destructor;
//                                                                        # e.g. ~X or ~X<N-1>
    private fun parseBaseUnresolvedName(): BaseNode? {
        if (Character.isDigit(peek())) {
            return parseSimpleId()
        } else if (consumeIf("dn")) {
            return parseDestructorName()
        }
        consumeIf("on")
        val operatorName = parseOperatorName(null) ?: return null
        if (peek() == 'I') {
            val templateArguments = parseTemplateArguments() ?: return null
            return NameTypeWithTemplateArguments(operatorName, templateArguments)
        }
        return operatorName
    }

    // <unresolved-name> ::= [gs] <base-unresolved-name>                     # x or (with "gs") ::x
//                   ::= sr <unresolved-type> <base-unresolved-name>     # T::x / decltype(p)::x
//                   ::= srN <unresolved-type> <unresolved-qualifier-level>+ E <base-unresolved-name>
//                                                                       # T::N::x /decltype(p)::N::x
//                   ::= [gs] sr <unresolved-qualifier-level>+ E <base-unresolved-name>
//                                                                       # A::x, N::y, A<T>::z; "gs" means leading "::"
    private fun parseUnresolvedName(): BaseNode? {
        var result: BaseNode? = null
        if (consumeIf("srN")) {
            result = parseUnresolvedType()
            if (result == null) {
                return null
            }
            if (peek() == 'I') {
                val templateArguments = parseTemplateArguments() ?: return null
                result = NameTypeWithTemplateArguments(result, templateArguments)
            }
            while (!consumeIf("E")) {
                val simpleId = parseSimpleId() ?: return null
                result = QualifiedName(result, simpleId)
            }
            val baseName = parseBaseUnresolvedName() ?: return null
            return QualifiedName(result, baseName)
        }
        val isGlobal = consumeIf("gs")
        // ::= [gs] <base-unresolved-name>                     # x or (with "gs") ::x
        if (!consumeIf("sr")) {
            result = parseBaseUnresolvedName()
            if (result == null) {
                return null
            }
            if (isGlobal) {
                result = GlobalQualifiedName(result)
            }
            return result
        }
        // ::= [gs] sr <unresolved-qualifier-level>+ E <base-unresolved-name>
        if (Character.isDigit(peek())) {
            do {
                val qualifier = parseSimpleId() ?: return null
                result = result?.let { QualifiedName(it, qualifier) }
                        ?: if (isGlobal) {
                            GlobalQualifiedName(qualifier)
                        } else {
                            qualifier
                        }
            } while (!consumeIf("E"))
        } else {
            result = parseUnresolvedType()
            if (result == null) {
                return null
            }
            if (peek() == 'I') {
                val templateArguments = parseTemplateArguments() ?: return null
                result = NameTypeWithTemplateArguments(result, templateArguments)
            }
        }
        val baseUnresolvedName = parseBaseUnresolvedName() ?: return null
        return QualifiedName(result, baseUnresolvedName)
    }

    //    <unscoped-name> ::= <unqualified-name>
//                    ::= St <unqualified-name>   # ::std::
    private fun parseUnscopedName(): BaseNode? {
        if (consumeIf("St")) {
            val unresolvedName = parseUnresolvedName() ?: return null
            return StdQualifiedName(unresolvedName)
        }
        return parseUnresolvedName()
    }

    // <nested-name> ::= N [<CV-qualifiers>] [<ref-qualifier>] <prefix (TODO)> <unqualified-name> E
//               ::= N [<CV-qualifiers>] [<ref-qualifier>] <template-prefix (TODO)> <template-args (TODO)> E
    private fun parseNestedName(context: NameparserContext?): BaseNode? { // Impossible in theory
        if (consume() != 'N') {
            return null
        }
        var result: BaseNode? = null
        val cv = CvType(parseCvQualifiers(), null)
        if (context != null) {
            context.cvType = cv
        }
        val ref = parseRefQualifiers()
        if (context != null) {
            context.ref = ref
        }
        if (consumeIf("St")) {
            result = NameType("std")
        }
        while (!consumeIf("E")) { // <data-member-prefix> end
            if (consumeIf("M")) {
                if (result == null) {
                    return null
                }
                continue
            }
            val c = peek()
            // TODO: template args
            if (c == 'T') {
                val templateParam = parseTemplateParam() ?: return null
                result = createNameNode(result, templateParam, context)
                substitutionList.add(result)
                continue
            }
            // <template-prefix> <template-args>
            if (c == 'I') {
                val templateArgument = parseTemplateArguments(context != null)
                if (templateArgument == null || result == null) {
                    return null
                }
                result = NameTypeWithTemplateArguments(result, templateArgument)
                if (context != null) {
                    context.finishWithTemplateArguments = true
                }
                substitutionList.add(result)
                continue
            }
            // <decltype>
            if (c == 'D' && (peek(1) == 't' || peek(1) == 'T')) {
                val decltype = parseDecltype() ?: return null
                result = createNameNode(result, decltype, context)
                substitutionList.add(result)
                continue
            }
            // <substitution>
            if (c == 'S' && peek(1) != 't') {
                val substitution = parseSubstitution() ?: return null
                result = createNameNode(result, substitution, context)
                if (result !== substitution) {
                    substitutionList.add(substitution)
                }
                continue
            }
            // <ctor-dtor-name> of parseUnqualifiedName
            if (c == 'C' || c == 'D' && peek(1) != 'C') { // We cannot have nothing before this
                if (result == null) {
                    return null
                }
                val ctOrDtorName = parseCtorDtorName(context, result) ?: return null
                result = createNameNode(result, ctOrDtorName, context)
                // TODO: ABI Tags (before)
                if (result == null) {
                    return null
                }
                substitutionList.add(result)
                continue
            }
            val unqualifiedName = parseUnqualifiedName(context) ?: return null
            result = createNameNode(result, unqualifiedName, context)
            substitutionList.add(result)
        }
        if (result == null || substitutionList.size == 0) {
            return null
        }
        substitutionList.removeAt(substitutionList.size - 1)
        return result
    }

    //   <discriminator> ::= _ <non-negative number>      # when number < 10
//                   ::= __ <non-negative number> _   # when number >= 10
    private fun parseDiscriminator() {
        if (count() == 0) {
            return
        }
        // We ignore the discriminator, we don't need it.
        if (consumeIf("_")) {
            consumeIf("_")
            while (Character.isDigit(peek()) && count() != 0) {
                consume()
            }
            consumeIf("_")
        }
    }

    //   <local-name> ::= Z <function encoding> E <entity name> [<discriminator>]
//                ::= Z <function encoding> E s [<discriminator>]
//                ::= Z <function encoding> Ed [ <parameter number> ] _ <entity name>
    private fun parseLocalName(context: NameparserContext?): BaseNode? {
        if (!consumeIf("Z")) {
            return null
        }
        val encoding = parseEncoding()
        if (encoding == null || !consumeIf("E")) {
            return null
        }
        val entityName: BaseNode?
        if (consumeIf("s")) {
            parseDiscriminator()
            return LocalName(encoding, NameType("String literal"))
        } else if (consumeIf("d")) {
            parseNumber(true)
            if (!consumeIf("_")) {
                return null
            }
            entityName = parseName(context)
            return entityName?.let { LocalName(encoding, it) }
        }
        entityName = parseName(context)
        if (entityName == null) {
            return null
        }
        parseDiscriminator()
        return LocalName(encoding, entityName)
    }

    // <name> ::= <nested-name>
//        ::= <unscoped-name>
//        ::= <unscoped-template-name> <template-args>
//        ::= <local-name>  # See Scope Encoding below (TODO)
    private fun parseName(context: NameparserContext? = null): BaseNode? {
        consumeIf("L")
        if (peek() == 'N') {
            return parseNestedName(context)
        }
        if (peek() == 'Z') {
            return parseLocalName(context)
        }
        if (peek() == 'S' && peek(1) != 't') {
            val substitution = parseSubstitution() ?: return null
            if (peek() != 'I') {
                return null
            }
            val templateArguments = parseTemplateArguments(context != null) ?: return null
            if (context != null) {
                context.finishWithTemplateArguments = true
            }
            return NameTypeWithTemplateArguments(substitution, templateArguments)
        }
        val result = parseUnscopedName() ?: return null
        if (peek() == 'I') {
            substitutionList.add(result)
            val templateArguments = parseTemplateArguments(context != null) ?: return null
            if (context != null) {
                context.finishWithTemplateArguments = true
            }
            return NameTypeWithTemplateArguments(result, templateArguments)
        }
        return result
    }

    private val isEncodingEnd: Boolean
        private get() {
            val c = peek()
            return count() == 0 || c == 'E' || c == '.' || c == '_'
        }

    // <encoding> ::= <function name> <bare-function-type>
//            ::= <data name>
//            ::= <special-name>
    private fun parseEncoding(): BaseNode? {
        val context = NameparserContext()
        if (peek() == 'T' || peek() == 'G' && peek(1) == 'V') {
            return parseSpecialName(context)
        }
        val name = parseName(context) ?: return null
        // TODO: compute template refs here
        if (isEncodingEnd) {
            return name
        }
        // TODO: Ua9enable_ifI
        var returnType: BaseNode? = null
        if (!context.ctorDtorConversion && context.finishWithTemplateArguments) {
            returnType = parseType()
            if (returnType == null) {
                return null
            }
        }
        if (consumeIf("v")) {
            return EncodedFunction(name, null, context.cvType, context.ref, null, returnType)
        }
        val params: MutableList<BaseNode?> = ArrayList()
        // backup because that can be destroyed by parseType
        val cv = context.cvType
        val ref = context.ref
        while (!isEncodingEnd) {
            val param = parseType() ?: return null
            params.add(param)
        }
        return EncodedFunction(name, NodeArray(params), cv, ref, null, returnType)
    }

    // <mangled-name> ::= _Z <encoding>
//                ::= <type>
    private fun parse(): BaseNode? {
        return if (consumeIf("_Z")) {
            val encoding = parseEncoding()
            if (encoding != null && count() == 0) {
                encoding
            } else null
        } else {
            val type = parseType()
            if (type != null && count() == 0) {
                type
            } else null
        }
    }

    companion object {
        private const val base36 = "0123456789abcdefghijklmnopqrstuvwxyz"
        private fun fromBase36(encoded: String): Int {
            val encodedArray = encoded.toLowerCase().toCharArray()
            val reversedEncoded = CharArray(encodedArray.size)
            //TODO: Check
            for (i in encodedArray.indices) {
                reversedEncoded[encodedArray.size - i - 1] = encodedArray[i]
            }
            var result = 0
            for (i in reversedEncoded.indices) {
                val value = base36.indexOf(reversedEncoded[i])
                if (value == -1) {
                    return -1
                }
                result += value * Math.pow(36.0, i.toDouble()).toInt()
            }
            return result
        }

        fun parse(originalMangled: String): String {
            val instance = Demangler(originalMangled)
            val resNode = instance.parse()
            if (resNode != null) {
                val writer = StringWriter()
                resNode.print(writer)
                return writer.toString()
            }
            return originalMangled
        }
    }

    init {
        length = mangled.length
        canParseTemplateArgs = true
    }
}