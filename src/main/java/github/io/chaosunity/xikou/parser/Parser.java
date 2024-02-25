package github.io.chaosunity.xikou.parser;

import github.io.chaosunity.xikou.lexer.Lexer;
import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.model.*;
import github.io.chaosunity.xikou.model.expr.Expr;
import github.io.chaosunity.xikou.model.expr.IntegerLiteral;
import github.io.chaosunity.xikou.model.types.AbstractTypeRef;
import github.io.chaosunity.xikou.model.types.PrimitiveType;
import github.io.chaosunity.xikou.model.types.PrimitiveTypeRef;
import github.io.chaosunity.xikou.model.types.TypeRef;

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
        int classCount = 0;
        ClassDecl[] classDecls = new ClassDecl[1];
        
        while (!lexer.peekToken(TokenType.EOF)) {
            int classModifiers = parseClassModifiers();
            
            if (lexer.acceptToken(TokenType.Class)) {
                if (classCount >= classDecls.length) {
                    ClassDecl[] newArr = new ClassDecl[classDecls.length * 2];
                    System.arraycopy(classDecls, 0, newArr, 0, classDecls.length);
                    classDecls = newArr;
                }
                
                classDecls[classCount++] = parseClassDecl(packageRef, classModifiers);
                continue;
            }
            
            if (lexer.acceptToken(TokenType.Impl)) {
                boolean bound = false;
                ImplDecl implDecl = parseImplDecl();
                
                for (int i = 0; i < classCount; i++) {
                    ClassDecl classDecl = classDecls[i];
                    
                    if (classDecl.className.literal.equals(implDecl.targetClass.literal)) {
                        classDecl.boundImplDecl = implDecl;
                        bound = true;
                        break;
                    }
                }
                
                if (!bound) {
                    throw new IllegalStateException(String.format("Unknown implementation to class %s", implDecl.targetClass.literal));
                }
                
                continue;
            }
            
            throw new IllegalStateException(String.format("Unexpected token %s", lexer.getCurrentToken().type));
        }
        
        return new XkFile(absoluteFilePath, packageRef, classCount, classDecls);
    }

    private PackageRef parsePackageRef() {
        StringBuilder qualifiedPackagePathBuilder = new StringBuilder();

        if (lexer.acceptToken(TokenType.Pkg)) {
            for (;;) {
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
        
        while (!lexer.peekToken(TokenType.CloseBrace)) {
            if (fieldCount >= fieldDecls.length) {
                FieldDecl[] newArr = new FieldDecl[fieldDecls.length * 2];
                System.arraycopy(fieldDecls, 0, newArr, 0, fieldDecls.length);
                fieldDecls = newArr;
            }

            fieldDecls[fieldCount++] = parseFieldDecl();
        }
        
        lexer.expectToken(TokenType.CloseBrace);
        
        return new ClassDecl(packageRef, modifiers, classNameToken, fieldCount, fieldDecls);
    }
    
    private AbstractTypeRef parseTypeRef() {
        int typeRefCount = 0;
        Token[] typeRefs = new Token[1];
        
        for (;;) {
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
        
        if (typeRefs.length == 1) {
            PrimitiveType[] primitiveTypes = PrimitiveType.values();
            
            for (int i = 0; i < primitiveTypes.length; i++) {
                PrimitiveType type = primitiveTypes[i];
                
                if (type.xkTypeName.equals(typeRefs[0].literal)) {
                    return new PrimitiveTypeRef(typeRefs[0], type);
                }
            }
        }
        
        return new TypeRef(typeRefCount, typeRefs);
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
            
            throw new IllegalStateException(String.format("Unexpected token %s while parsing implementation", lexer.getCurrentToken().type));
        }
        
        lexer.expectToken(TokenType.CloseBrace);
    
        return new ImplDecl(targetClass, primaryConstructorDecl);
    }
    
    private PrimaryConstructorDecl parsePrimaryConstructorDecl(int modifiers) {
        lexer.expectToken(TokenType.OpenParenthesis);
        
        Parameters parameters = parseParameters();
        
        return new PrimaryConstructorDecl(modifiers, parameters);
    }
    
    private Parameters parseParameters() {
        int parameterCount = 0;
        Parameter[] parameters = new Parameter[1];
        
        for (;;) {
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
                
                if (lexer.acceptToken(TokenType.CloseParenthesis)) {
                    break;
                } else {
                    lexer.expectToken(TokenType.Comma);
                }
            } else {
                lexer.expectToken(TokenType.CloseParenthesis);
                break;
            }
        }
        
        return new Parameters(parameterCount, parameters);
    }
    
    private Expr parseExpr() {
        return parseLiteralExpr();
    }
    
    private Expr parseLiteralExpr() {
        if (lexer.peekToken(TokenType.NumberLiteral)) {
            return new IntegerLiteral(lexer.expectToken(TokenType.NumberLiteral));
        }
        
        throw new IllegalStateException(String.format("Unexpected expression token %s", lexer.getCurrentToken().type));
    }
}
