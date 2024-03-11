package github.io.chaosunity.xikou.parser;

import github.io.chaosunity.xikou.ast.*;
import github.io.chaosunity.xikou.ast.expr.*;
import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.ast.types.ArrayTypeRef;
import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
import github.io.chaosunity.xikou.ast.types.PrimitiveTypeRef;
import github.io.chaosunity.xikou.lexer.Lexer;
import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Parser {
    private final Path absoluteFilePath;
    private final Lexer lexer;

    public Parser(Path absoluteFilePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(absoluteFilePath);
        String source = new String(fileBytes, StandardCharsets.UTF_8);
        this.absoluteFilePath = absoluteFilePath;
        this.lexer = new Lexer(source);
    }

    public XkFile parseFile() {
        PackageRef packageRef = parsePackageRef();
        int declCount = 0;
        BoundableDecl[] decls = new BoundableDecl[1];

        while (!lexer.peekToken(TokenType.EOF)) {
            int declModifiers = parseClassModifiers();

            if (lexer.acceptToken(TokenType.Class)) {
                if (declCount >= decls.length) {
                    BoundableDecl[] newArr = new BoundableDecl[decls.length * 2];
                    System.arraycopy(decls, 0, newArr, 0, decls.length);
                    decls = newArr;
                }

                decls[declCount++] = parseClassDecl(packageRef, declModifiers);
                continue;
            }

            if (lexer.acceptToken(TokenType.Enum)) {
                if (declCount >= decls.length) {
                    BoundableDecl[] newArr = new BoundableDecl[decls.length * 2];
                    System.arraycopy(decls, 0, newArr, 0, decls.length);
                    decls = newArr;
                }

                decls[declCount++] = parseEnumDecl(packageRef, declModifiers);
                continue;
            }

            if (lexer.acceptToken(TokenType.Impl)) {
                boolean bound = false;
                ImplDecl implDecl = parseImplDecl();

                for (int i = 0; i < declCount; i++) {
                    BoundableDecl classDecl = decls[i];

                    if (classDecl.getName().equals(implDecl.targetClass.literal)) {
                        classDecl.bindImplbidirectionally(implDecl);
                        bound = true;
                        break;
                    }
                }

                if (!bound) throw new IllegalStateException(
                        String.format("Unknown implementation to class %s",
                                      implDecl.targetClass.literal));

                continue;
            }

            throw new IllegalStateException(
                    String.format("Unexpected token %s", lexer.getCurrentToken().type));
        }

        return new XkFile(absoluteFilePath, packageRef, declCount, decls);
    }

    private PackageRef parsePackageRef() {
        StringBuilder qualifiedPackagePathBuilder = new StringBuilder();

        if (lexer.acceptToken(TokenType.Pkg)) {
            for (; ; ) {
                Token indent = lexer.expectToken(TokenType.Identifier);

                qualifiedPackagePathBuilder.append(indent.literal);

                if (lexer.acceptToken(TokenType.DoubleColon)) {
                    qualifiedPackagePathBuilder.append(".");
                } else {
                    lexer.expectToken(TokenType.SemiColon);
                    break;
                }
            }
        }

        return new PackageRef(qualifiedPackagePathBuilder.toString());
    }

    private int parseAccessModifiers() {
        int modifiers = 0;

        if (lexer.acceptToken(TokenType.Pub)) {
            if (lexer.acceptToken(TokenType.OpenParenthesis)) {
                lexer.expectToken(TokenType.Pkg);
                lexer.expectToken(TokenType.CloseParenthesis);
            } else {
                modifiers |= Modifier.PUBLIC;
            }
        } else if (lexer.acceptToken(TokenType.Priv)) {
            modifiers |= Modifier.PRIVATE;
        }

        return modifiers;
    }

    private int parseClassModifiers() {
        int modifiers = 0;

        modifiers |= parseAccessModifiers();

        return modifiers;
    }

    private int parseFieldModifiers() {
        int modifiers = 0;

        modifiers |= parseAccessModifiers();

        if (!lexer.acceptToken(TokenType.Mut)) {
            modifiers |= Modifier.FINAL;
        }

        return modifiers;
    }

    private ClassDecl parseClassDecl(PackageRef packageRef, int modifiers) {
        Token classNameToken = lexer.expectToken(TokenType.Identifier);
        int fieldCount = 0;
        FieldDecl[] fieldDecls = new FieldDecl[1];

        lexer.expectToken(TokenType.OpenBrace);

        while (!lexer.acceptToken(TokenType.CloseBrace)) {
            if (fieldCount >= fieldDecls.length) {
                FieldDecl[] newArr = new FieldDecl[fieldDecls.length * 2];
                System.arraycopy(fieldDecls, 0, newArr, 0, fieldDecls.length);
                fieldDecls = newArr;
            }

            fieldDecls[fieldCount++] = parseFieldDecl();
        }

        return new ClassDecl(packageRef, modifiers, classNameToken, fieldCount, fieldDecls);
    }

    private EnumDecl parseEnumDecl(PackageRef packageRef, int modifiers) {
        Token enumNameToken = lexer.expectToken(TokenType.Identifier), cachedIdentifierToken = null;
        int fieldCount = 0;
        FieldDecl[] fieldDecls = new FieldDecl[1];
        int enumVariantCount = 0;
        EnumVariantDecl[] enumVariantDecls = new EnumVariantDecl[1];

        lexer.expectToken(TokenType.OpenBrace);

        while (!lexer.peekToken(TokenType.CloseBrace)) {
            int fieldModifiers = parseFieldModifiers();

            cachedIdentifierToken = lexer.expectToken(TokenType.Identifier);

            if (!lexer.acceptToken(TokenType.Colon)) break;

            AbstractTypeRef fieldTypeRef = parseTypeRef();

            lexer.expectToken(TokenType.SemiColon);

            if (fieldCount >= fieldDecls.length) {
                FieldDecl[] newArr = new FieldDecl[fieldDecls.length * 2];
                System.arraycopy(fieldDecls, 0, newArr, 0, fieldDecls.length);
                fieldDecls = newArr;
            }

            fieldDecls[fieldCount++] = new FieldDecl(fieldModifiers, cachedIdentifierToken,
                                                     fieldTypeRef, null, null);
        }

        while (!lexer.peekToken(TokenType.CloseBrace)) {
            if (enumVariantCount >= enumVariantDecls.length) {
                EnumVariantDecl[] newArr = new EnumVariantDecl[enumVariantDecls.length * 2];
                System.arraycopy(enumVariantDecls, 0, newArr, 0, enumVariantDecls.length);
                enumVariantDecls = newArr;
            }

            enumVariantDecls[enumVariantCount++] = parseEnumVariantDecl(cachedIdentifierToken);
            cachedIdentifierToken = null;

            if (!lexer.peekToken(TokenType.CloseBrace)) {
                lexer.expectToken(TokenType.Comma);
            }
        }

        lexer.expectToken(TokenType.CloseBrace);

        return new EnumDecl(packageRef, modifiers, enumNameToken, fieldCount, fieldDecls,
                            enumVariantCount, enumVariantDecls);
    }

    private EnumVariantDecl parseEnumVariantDecl(Token cachedIdentifierToken) {
        Token variantNameToken = cachedIdentifierToken != null ? cachedIdentifierToken : lexer.expectToken(
                TokenType.Identifier);
        Arguments arguments;

        if (lexer.acceptToken(TokenType.OpenParenthesis) && !lexer.acceptToken(
                TokenType.CloseParenthesis)) {
            arguments = parseArguments();
        } else arguments = new Arguments(0, new Expr[0]);

        return new EnumVariantDecl(variantNameToken, arguments.argumentCount, arguments.arguments);
    }

    private FieldDecl parseFieldDecl() {
        int fieldModifiers = parseFieldModifiers();
        Token fieldNameToken = lexer.expectToken(TokenType.Identifier);

        lexer.expectToken(TokenType.Colon);

        AbstractTypeRef typeRef = parseTypeRef();
        Token equalToken = null;
        Expr initialExpr = null;

        if (lexer.peekToken(TokenType.Equal)) {
            equalToken = lexer.expectToken(TokenType.Equal);
            initialExpr = parseExpr();
        }

        lexer.expectToken(TokenType.SemiColon);

        return new FieldDecl(fieldModifiers, fieldNameToken, typeRef, equalToken, initialExpr);
    }

    private ImplDecl parseImplDecl() {
        Token targetClass = lexer.expectToken(TokenType.Identifier);
        PrimaryConstructorDecl primaryConstructorDecl = null;

        lexer.expectToken(TokenType.OpenBrace);

        while (!lexer.peekToken(TokenType.CloseBrace)) {
            int modifiers = parseAccessModifiers();

            if (lexer.acceptToken(TokenType.Self)) {
                primaryConstructorDecl = parsePrimaryConstructorDecl(modifiers);
                continue;
            }

            throw new IllegalStateException(
                    String.format("Unexpected token %s while parsing implementation",
                                  lexer.getCurrentToken().type));
        }

        lexer.expectToken(TokenType.CloseBrace);

        return new ImplDecl(targetClass, primaryConstructorDecl);
    }

    private PrimaryConstructorDecl parsePrimaryConstructorDecl(int modifiers) {
        lexer.expectToken(TokenType.OpenParenthesis);

        Parameters parameters = parseParameters();

        int exprCount = 0;
        Expr[] exprs = new Expr[1];

        lexer.expectToken(TokenType.OpenBrace);

        while (!lexer.acceptToken(TokenType.CloseBrace)) {
            if (exprCount >= exprs.length) {
                Expr[] newArr = new Expr[exprs.length * 2];
                System.arraycopy(exprs, 0, newArr, 0, exprs.length);
                exprs = newArr;
            }

            exprs[exprCount++] = parseExpr();
            lexer.expectToken(TokenType.SemiColon);
        }

        return new PrimaryConstructorDecl(modifiers, parameters.parameterCount,
                                          parameters.parameters, exprCount, exprs);
    }

    private Parameters parseParameters() {
        int parameterCount = 0;
        Parameter[] parameters = new Parameter[1];

        while (!lexer.acceptToken(TokenType.CloseParenthesis)) {
            if (lexer.peekToken(TokenType.Identifier)) {
                Token name = lexer.expectToken(TokenType.Identifier);
                lexer.expectToken(TokenType.Colon);
                AbstractTypeRef typeRef = parseTypeRef();

                if (parameterCount >= parameters.length) {
                    Parameter[] newArr = new Parameter[parameters.length * 2];
                    System.arraycopy(parameters, 0, newArr, 0, parameters.length);
                    parameters = newArr;
                }

                parameters[parameterCount++] = new Parameter(name, typeRef);

                if (!lexer.peekToken(TokenType.CloseParenthesis))
                    lexer.expectToken(TokenType.Comma);
            }
        }

        return new Parameters(parameterCount, parameters);
    }

    private Arguments parseArguments() {
        int argumentCount = 0;
        Expr[] arguments = new Expr[1];

        while (!lexer.acceptToken(TokenType.CloseParenthesis)) {
            if (argumentCount >= arguments.length) {
                Expr[] newArr = new Expr[arguments.length * 2];
                System.arraycopy(arguments, 0, newArr, 0, arguments.length);
                arguments = newArr;
            }

            arguments[argumentCount++] = parseExpr();

            if (!lexer.peekToken(TokenType.CloseParenthesis)) lexer.expectToken(TokenType.Comma);
        }

        return new Arguments(argumentCount, arguments);
    }

    private Expr parseExpr() {
        return parseInfixExpr(0);
    }

    private Expr parseInfixExpr(int parentPrecedence) {
        Expr lhs = parseDotSuffixExpr();

        while (true) {
            Token operatorToken = lexer.getCurrentToken();
            int precedence = operatorToken.type.getInfixPrecedence();
            if (precedence == 0 || precedence <= parentPrecedence) break;

            lexer.advanceToken();
            Expr rhs = parseInfixExpr(precedence);
            lhs = new InfixExpr(lhs, operatorToken, rhs);
        }

        return lhs;
    }

    private Expr parseDotSuffixExpr() {
        Expr lhs = parsePrimaryStart();

        while (lexer.acceptToken(TokenType.Dot)) {
            if (lexer.peekToken(TokenType.Identifier)) {
                // (Instance / Static) Member access / Method call
                Token memberNameToken = lexer.expectToken(TokenType.Identifier);

                if (lexer.acceptToken(TokenType.OpenParenthesis)) {
                    // (Instance / Static) Method call
                } else {
                    // (Instance / Static) Member access
                    lhs = new MemberAccessExpr(lhs, memberNameToken);
                }
            } else {
                // Constructor call, e.g. Integer.self(...)
                Arguments arguments;
                if (!(lhs instanceof TypeableExpr)) throw new IllegalStateException(
                        "Unable to invoke constructor with non typeable expression");

                lexer.expectToken(TokenType.Self);
                lexer.expectToken(TokenType.OpenParenthesis);

                if (!lexer.acceptToken(TokenType.CloseParenthesis)) arguments = parseArguments();
                else arguments = new Arguments(0, new Expr[0]);

                lhs = new ConstructorCallExpr((TypeableExpr) lhs, arguments.argumentCount,
                                              arguments.arguments);

            }
        }

        return lhs;
    }

    private Expr parsePrimaryStart() {
        if (lexer.peekToken(TokenType.OpenBracket)) return parseArrayInitExpr();

        if (lexer.peekToken(TokenType.Identifier)) {
            int identifierStartPos = lexer.getPos();
            Token identifierStartToken = lexer.getCurrentToken();
            AbstractTypeRef typeRef = parseNonArrayTypeRef();

            if (typeRef instanceof ClassTypeRef) {
                ClassTypeRef classTypeRef = (ClassTypeRef) typeRef;

                if (classTypeRef.selectorCount > 1) {
                    return new TypeExpr(classTypeRef);
                }
            }

            lexer.rewindPos(identifierStartPos, identifierStartToken);

            return parseNameExpr();
        }

        if (lexer.peekToken(TokenType.Self)) {
            return parseSelfNameExpr();
        }

        if (lexer.peekToken(TokenType.NumberLiteral)) {
            return new IntegerLiteral(lexer.expectToken(TokenType.NumberLiteral));
        }

        throw new IllegalStateException(
                String.format("Unexpected expression token %s", lexer.getCurrentToken().type));
    }

    private ArrayInitExpr parseArrayInitExpr() {
        lexer.expectToken(TokenType.OpenBracket);

        AbstractTypeRef componentTypeRef = parseTypeRef();
        Expr sizeExpr = null;
        int initExprCount = 0;
        Expr[] initExprs = new Expr[1];

        lexer.expectToken(TokenType.SemiColon);

        if (!lexer.peekToken(TokenType.CloseBracket)) sizeExpr = parseExpr();

        lexer.expectToken(TokenType.CloseBracket);

        if (lexer.acceptToken(TokenType.OpenBrace)) {
            while (!lexer.acceptToken(TokenType.CloseBrace)) {
                if (initExprCount >= initExprs.length) {
                    Expr[] newArr = new Expr[initExprs.length * 2];
                    System.arraycopy(initExprs, 0, newArr, 0, initExprs.length);
                    initExprs = newArr;
                }

                initExprs[initExprCount++] = parseExpr();

                if (!lexer.peekToken(TokenType.CloseBrace)) lexer.expectToken(TokenType.Comma);
            }
        }

        return new ArrayInitExpr(componentTypeRef, sizeExpr, initExprCount, initExprs);
    }

    private NameExpr parseNameExpr() {
        return new NameExpr(lexer.expectToken(TokenType.Identifier));
    }

    private NameExpr parseSelfNameExpr() {
        return new NameExpr(lexer.expectToken(TokenType.Self));
    }

    private AbstractTypeRef parseTypeRef() {
        if (lexer.peekToken(TokenType.OpenBracket)) return parseArrayTypeRef();
        else return parseNonArrayTypeRef();
    }

    private ArrayTypeRef parseArrayTypeRef() {
        lexer.expectToken(TokenType.OpenBracket);
        AbstractTypeRef componentType = parseTypeRef();
        lexer.expectToken(TokenType.CloseBracket);

        return new ArrayTypeRef(componentType);
    }

    private AbstractTypeRef parseNonArrayTypeRef() {
        ClassTypeRef classTypeRef = parseClassTypeRef();

        if (classTypeRef.selectorCount == 1) {
            PrimitiveType[] primitiveTypes = PrimitiveType.values();

            for (PrimitiveType type : primitiveTypes) {
                if (type.xkTypeName.equals(classTypeRef.selectors[0].literal)) {
                    return new PrimitiveTypeRef(classTypeRef.selectors[0], type);
                }
            }
        }

        return classTypeRef;
    }

    private ClassTypeRef parseClassTypeRef() {
        int typeRefCount = 0;
        Token[] typeRefs = new Token[1];

        while (true) {
            if (typeRefCount >= typeRefs.length) {
                Token[] newArr = new Token[typeRefs.length * 2];
                System.arraycopy(typeRefs, 0, newArr, 0, typeRefs.length);
                typeRefs = newArr;
            }

            typeRefs[typeRefCount++] = lexer.expectToken(TokenType.Identifier);

            if (!lexer.acceptToken(TokenType.DoubleColon)) {
                break;
            }
        }

        return new ClassTypeRef(typeRefCount, typeRefs);
    }
}
