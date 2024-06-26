package github.io.chaosunity.xikou.parser;

import github.io.chaosunity.xikou.ast.*;
import github.io.chaosunity.xikou.ast.expr.*;
import github.io.chaosunity.xikou.ast.stmt.ExprStmt;
import github.io.chaosunity.xikou.ast.stmt.Statement;
import github.io.chaosunity.xikou.ast.stmt.VarDeclStmt;
import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.ast.types.ArrayTypeRef;
import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
import github.io.chaosunity.xikou.ast.types.PrimitiveTypeRef;
import github.io.chaosunity.xikou.lexer.Lexer;
import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

public class Parser {

  private final Path absoluteFilePath;
  private final Lexer lexer;

  public Parser(Path absoluteFilePath, String source) {
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

        if (!bound) {
          throw new IllegalStateException(
              String.format("Unknown implementation to class %s", implDecl.targetClass.literal));
        }

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
    int inheritedCount = 0;
    ClassTypeRef[] inheritedClasses = new ClassTypeRef[1];
    int fieldCount = 0;
    FieldDecl[] fieldDecls = new FieldDecl[1];

    if (lexer.acceptToken(TokenType.Colon)) {
      while (true) {
        if (inheritedCount >= inheritedClasses.length) {
          ClassTypeRef[] newArr = new ClassTypeRef[inheritedClasses.length * 2];
          System.arraycopy(inheritedClasses, 0, newArr, 0, inheritedClasses.length);
          inheritedClasses = newArr;
        }

        AbstractTypeRef typeRef = parseTypeRef();

        if (typeRef instanceof ArrayTypeRef) {
          throw new IllegalStateException("Cannot extends an array type");
        }
        if (typeRef instanceof PrimitiveTypeRef) {
          throw new IllegalStateException("Cannot extends a primitive type");
        }

        inheritedClasses[inheritedCount++] = (ClassTypeRef) typeRef;

        if (lexer.peekToken(TokenType.OpenBrace)) {
          break;
        } else {
          lexer.expectToken(TokenType.Comma);
        }
      }
    }

    lexer.expectToken(TokenType.OpenBrace);

    while (!lexer.acceptToken(TokenType.CloseBrace)) {
      if (fieldCount >= fieldDecls.length) {
        FieldDecl[] newArr = new FieldDecl[fieldDecls.length * 2];
        System.arraycopy(fieldDecls, 0, newArr, 0, fieldDecls.length);
        fieldDecls = newArr;
      }

      fieldDecls[fieldCount++] = parseFieldDecl();
    }

    return new ClassDecl(
        packageRef,
        modifiers,
        classNameToken,
        inheritedCount,
        inheritedClasses,
        fieldCount,
        fieldDecls);
  }

  private EnumDecl parseEnumDecl(PackageRef packageRef, int modifiers) {
    Token enumNameToken = lexer.expectToken(TokenType.Identifier), cachedIdentifierToken = null;
    int interfaceCount = 0;
    ClassTypeRef[] interfaces = new ClassTypeRef[1];
    int fieldCount = 0;
    FieldDecl[] fieldDecls = new FieldDecl[1];
    int enumVariantCount = 0;
    EnumVariantDecl[] enumVariantDecls = new EnumVariantDecl[1];

    if (lexer.acceptToken(TokenType.Colon)) {
      while (true) {
        if (interfaceCount >= interfaces.length) {
          ClassTypeRef[] newArr = new ClassTypeRef[interfaces.length * 2];
          System.arraycopy(interfaces, 0, newArr, 0, interfaces.length);
          interfaces = newArr;
        }

        AbstractTypeRef typeRef = parseTypeRef();

        if (typeRef instanceof ArrayTypeRef) {
          throw new IllegalStateException("Cannot extends an array type");
        }
        if (typeRef instanceof PrimitiveTypeRef) {
          throw new IllegalStateException("Cannot extends a primitive type");
        }

        interfaces[interfaceCount++] = (ClassTypeRef) typeRef;

        if (lexer.peekToken(TokenType.OpenBrace)) {
          break;
        } else {
          lexer.expectToken(TokenType.Comma);
        }
      }
    }

    lexer.expectToken(TokenType.OpenBrace);

    while (!lexer.peekToken(TokenType.CloseBrace)) {
      int fieldModifiers = parseFieldModifiers();

      cachedIdentifierToken = lexer.expectToken(TokenType.Identifier);

      if (!lexer.acceptToken(TokenType.Colon)) {
        break;
      }

      AbstractTypeRef fieldTypeRef = parseTypeRef();

      lexer.expectToken(TokenType.SemiColon);

      if (fieldCount >= fieldDecls.length) {
        FieldDecl[] newArr = new FieldDecl[fieldDecls.length * 2];
        System.arraycopy(fieldDecls, 0, newArr, 0, fieldDecls.length);
        fieldDecls = newArr;
      }

      fieldDecls[fieldCount++] =
          new FieldDecl(fieldModifiers, cachedIdentifierToken, fieldTypeRef, null, null);
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

    return new EnumDecl(
        packageRef,
        modifiers,
        enumNameToken,
        interfaceCount,
        interfaces,
        fieldCount,
        fieldDecls,
        enumVariantCount,
        enumVariantDecls);
  }

  private EnumVariantDecl parseEnumVariantDecl(Token cachedIdentifierToken) {
    Token variantNameToken =
        cachedIdentifierToken != null
            ? cachedIdentifierToken
            : lexer.expectToken(TokenType.Identifier);
    Arguments arguments;

    if (lexer.acceptToken(TokenType.OpenParenthesis)
        && !lexer.acceptToken(TokenType.CloseParenthesis)) {
      arguments = parseArguments();
    } else {
      arguments = new Arguments(0, new Expr[0]);
    }

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
    int constCount = 0;
    ConstDecl[] constDecls = new ConstDecl[1];
    ConstructorDecl constructorDecl = null;
    int functionCount = 0;
    FnDecl[] functionDecls = new FnDecl[1];

    lexer.expectToken(TokenType.OpenBrace);

    while (!lexer.peekToken(TokenType.CloseBrace)) {
      int modifiers = parseAccessModifiers();

      if (lexer.acceptToken(TokenType.Const)) {
        if (constCount >= constDecls.length) {
          ConstDecl[] newArr = new ConstDecl[constDecls.length * 2];
          System.arraycopy(constDecls, 0, newArr, 0, constDecls.length);
          constDecls = newArr;
        }

        constDecls[constCount++] = parseConstDecl(modifiers);
        continue;
      }

      if (lexer.acceptToken(TokenType.Self)) {
        constructorDecl = parseConstructorDecl(modifiers);
        continue;
      }

      if (lexer.acceptToken(TokenType.Fn)) {
        if (functionCount >= functionDecls.length) {
          FnDecl[] newArr = new FnDecl[functionDecls.length * 2];
          System.arraycopy(functionDecls, 0, newArr, 0, functionDecls.length);
          functionDecls = newArr;
        }

        functionDecls[functionCount++] = parseFunctionDecl(modifiers);
        continue;
      }

      throw new IllegalStateException(
          String.format(
              "Unexpected token %s while parsing implementation", lexer.getCurrentToken().type));
    }

    lexer.expectToken(TokenType.CloseBrace);

    return new ImplDecl(
        targetClass, constCount, constDecls, constructorDecl, functionCount, functionDecls);
  }

  private ConstDecl parseConstDecl(int modifiers) {
    Token nameToken = lexer.expectToken(TokenType.Identifier);

    lexer.expectToken(TokenType.Colon);

    AbstractTypeRef explicitTypeRef = parseTypeRef();

    lexer.expectToken(TokenType.Equal);

    Expr initialExpr = parseExpr();

    lexer.expectToken(TokenType.SemiColon);

    return new ConstDecl(modifiers, nameToken, explicitTypeRef, initialExpr);
  }

  private ConstructorDecl parseConstructorDecl(int modifiers) {
    lexer.expectToken(TokenType.OpenParenthesis);

    Parameters parameters = parseParameters();

    int statementCount = 0;
    Statement[] statements = new Statement[1];

    lexer.expectToken(TokenType.OpenBrace);

    while (!lexer.acceptToken(TokenType.CloseBrace)) {
      if (statementCount >= statements.length) {
        Statement[] newArr = new Statement[statements.length * 2];
        System.arraycopy(statements, 0, newArr, 0, statements.length);
        statements = newArr;
      }

      statements[statementCount++] = parseStatement();
    }

    return new ConstructorDecl(
        modifiers, parameters.parameterCount, parameters.parameters, statementCount, statements);
  }

  private FnDecl parseFunctionDecl(int modifiers) {
    Token nameToken = lexer.expectToken(TokenType.Identifier);
    Token selfToken = null;
    lexer.expectToken(TokenType.OpenParenthesis);

    if (lexer.peekToken(TokenType.Self)) {
      selfToken = lexer.advanceToken();

      if (!lexer.peekToken(TokenType.CloseParenthesis)) {
        lexer.expectToken(TokenType.Comma);
      }
    }

    Parameters parameters = parseParameters();

    AbstractTypeRef returnTypeRef = null;

    if (lexer.acceptToken(TokenType.SlimArrow)) {
      returnTypeRef = parseTypeRef();
    }

    int statementCount = 0;
    Statement[] statements = new Statement[1];

    lexer.expectToken(TokenType.OpenBrace);

    while (!lexer.acceptToken(TokenType.CloseBrace)) {
      if (statementCount >= statements.length) {
        Statement[] newArr = new Statement[statements.length * 2];
        System.arraycopy(statements, 0, newArr, 0, statements.length);
        statements = newArr;
      }

      statements[statementCount++] = parseStatement();
    }

    return new FnDecl(
        modifiers,
        nameToken,
        selfToken,
        parameters.parameterCount,
        parameters.parameters,
        returnTypeRef,
        statementCount,
        statements);
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

        if (!lexer.peekToken(TokenType.CloseParenthesis)) {
          lexer.expectToken(TokenType.Comma);
        }
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

      if (!lexer.peekToken(TokenType.CloseParenthesis)) {
        lexer.expectToken(TokenType.Comma);
      }
    }

    return new Arguments(argumentCount, arguments);
  }

  private Statement parseStatement() {
    if (lexer.acceptToken(TokenType.Let)) {
      // Variable Declaration
      Token mutToken = null;
      Expr initialExpr = null;

      if (lexer.peekToken(TokenType.Mut)) {
        mutToken = lexer.advanceToken();
      }

      Token varNameToken = lexer.expectToken(TokenType.Identifier);

      if (lexer.acceptToken(TokenType.Equal)) {
        initialExpr = parseExpr();
      }

      lexer.expectToken(TokenType.SemiColon);

      return new VarDeclStmt(mutToken, varNameToken, initialExpr);
    } else {
      Expr expr = parseExpr();
      Token semicolon = null;

      if (lexer.peekToken(TokenType.SemiColon)) {
        semicolon = lexer.advanceToken();
      }

      return new ExprStmt(expr, semicolon);
    }
  }

  private Expr parseExpr() {
    return parseInfixExpr(0);
  }

  private Expr parseInfixExpr(int parentPrecedence) {
    Expr lhs = parseSuffixExpr();

    while (!lexer.peekToken(TokenType.EOF)) {
      Token operatorToken = lexer.getCurrentToken();
      int precedence = operatorToken.type.getInfixPrecedence();
      if (precedence == 0 || precedence <= parentPrecedence) {
        break;
      }

      lexer.advanceToken();

      if (operatorToken.type == TokenType.As) {
        AbstractTypeRef targetTypeRef = parseTypeRef();

        lhs = new CastExpr(lhs, targetTypeRef);
        continue;
      }

      Expr rhs = parseInfixExpr(precedence);

      switch (operatorToken.type) {
        case Plus:
        case Minus:
          lhs = new ArithmeticExpr(lhs, operatorToken, rhs);
          break;
        case DoubleAmpersand:
        case DoublePipe:
          {
            int exprCount = 2;
            Expr[] exprs = {lhs, rhs};

            while (lexer.acceptToken(operatorToken.type)) {
              if (exprCount >= exprs.length) {
                Expr[] newArr = new Expr[exprCount * 2];
                System.arraycopy(exprs, 0, newArr, 0, exprCount);
                exprs = newArr;
              }

              exprs[exprCount++] = parseInfixExpr(precedence);
            }

            lhs = new CondExpr(exprCount, exprs, operatorToken);
            break;
          }
        case DoubleEqual:
        case NotEqual:
        case Greater:
        case GreaterEqual:
        case Lesser:
        case LesserEqual:
          lhs = new CompareExpr(lhs, operatorToken, rhs);
          break;
        case Equal:
        case PlusEqual:
        case MinusEqual:
          {
            int targetCount = 2;
            Expr[] targets = {lhs, rhs};

            while (lexer.acceptToken(operatorToken.type)) {
              if (targetCount >= targets.length) {
                Expr[] newArr = new Expr[targetCount * 2];
                System.arraycopy(targets, 0, newArr, 0, targetCount);
                targets = newArr;
              }

              targets[targetCount++] = parseInfixExpr(precedence);
            }

            rhs = targets[targetCount - 1];
            targets[--targetCount] = null;

            lhs = new AssignmentExpr(targetCount, targets, operatorToken, rhs);
            break;
          }
        default:
          throw new IllegalStateException(
              String.format("ICE: %s is not a valid infix operator", operatorToken.type));
      }
    }

    return lhs;
  }

  private Expr parseSuffixExpr() {
    Expr lhs = parsePrimaryStart();

    while (true) {
      if (lexer.acceptToken(TokenType.Dot)) {
        lhs = parseDotSuffixExpr(lhs);
      } else if (lexer.acceptToken(TokenType.OpenBracket)) {
        Expr indexExpr = parseExpr();
        lexer.expectToken(TokenType.CloseBracket);

        lhs = new IndexExpr(lhs, indexExpr);
      } else if (lhs instanceof NameExpr && lexer.acceptToken(TokenType.OpenParenthesis)) {
        // (Instance / Static) Method call
        Arguments arguments;

        if (!lexer.acceptToken(TokenType.CloseParenthesis)) {
          arguments = parseArguments();
        } else {
          arguments = new Arguments(0, new Expr[0]);
        }

        lhs =
            new MethodCallExpr(
                null, ((NameExpr) lhs).varIdentifier, arguments.argumentCount, arguments.arguments);
      } else {
        break;
      }
    }

    return lhs;
  }

  private Expr parseDotSuffixExpr(Expr lhs) {
    if (lexer.peekToken(TokenType.Identifier)) {
      // (Instance / Static) Member access / Method call
      Token memberNameToken = lexer.expectToken(TokenType.Identifier);

      if (lexer.acceptToken(TokenType.OpenParenthesis)) {
        // (Instance / Static) Method call
        Arguments arguments;

        if (!lexer.acceptToken(TokenType.CloseParenthesis)) {
          arguments = parseArguments();
        } else {
          arguments = new Arguments(0, new Expr[0]);
        }

        return new MethodCallExpr(
            lhs, memberNameToken, arguments.argumentCount, arguments.arguments);
      } else {
        // (Instance / Static) Member access
        return new FieldAccessExpr(lhs, memberNameToken);
      }
    } else {
      // Constructor call, e.g. Integer.self(...)
      Arguments arguments;
      if (!(lhs instanceof TypeableExpr)) {
        throw new IllegalStateException(
            "Unable to invoke constructor with non typeable expression");
      }

      lexer.expectToken(TokenType.Self);
      lexer.expectToken(TokenType.OpenParenthesis);

      if (!lexer.acceptToken(TokenType.CloseParenthesis)) {
        arguments = parseArguments();
      } else {
        arguments = new Arguments(0, new Expr[0]);
      }

      return new ConstructorCallExpr(
          (TypeableExpr) lhs, arguments.argumentCount, arguments.arguments);
    }
  }

  private Expr parsePrimaryStart() {
    if (lexer.peekToken(TokenType.OpenBracket)) {
      return parseArrayInitExpr();
    }

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

    if (lexer.acceptToken(TokenType.Return)) {
      Expr expr = parseExpr();

      return new ReturnExpr(expr);
    }

    if (lexer.acceptToken(TokenType.If)) {
      Expr condExpr = parseExpr();
      BlockExpr trueBranchExpr = parseBlockExpr();
      Expr falseBranchExpr = null;

      if (lexer.acceptToken(TokenType.Else)) {
        falseBranchExpr = parseExpr();
      }

      return new IfExpr(condExpr, trueBranchExpr, falseBranchExpr);
    }

    if (lexer.acceptToken(TokenType.While)) {
      Expr condExpr = parseExpr();
      BlockExpr iterExpr = parseBlockExpr();

      return new WhileExpr(condExpr, iterExpr);
    }

    if (lexer.acceptToken(TokenType.For)) {
      NameExpr iterateVarNameExpr = parseNameExpr();

      lexer.expectToken(TokenType.In);

      Expr iteratableTargetExpr = parseExpr();
      BlockExpr iterationBlock = parseBlockExpr();

      return new ForExpr(iterateVarNameExpr, iteratableTargetExpr, iterationBlock);
    }

    if (lexer.peekToken(TokenType.OpenBrace)) {
      return parseBlockExpr();
    }

    if (lexer.peekToken(TokenType.CharLiteral)) {
      return new CharLiteralExpr(lexer.expectToken(TokenType.CharLiteral));
    }

    if (lexer.peekToken(TokenType.StringLiteral)) {
      return new StringLiteralExpr(lexer.expectToken(TokenType.StringLiteral));
    }

    if (lexer.peekToken(TokenType.NumberLiteral)) {
      return new IntegerLiteralExpr(lexer.expectToken(TokenType.NumberLiteral));
    }

    if (lexer.peekToken(TokenType.Null)) {
      return new NullLiteral(lexer.expectToken(TokenType.Null));
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

    if (!lexer.peekToken(TokenType.CloseBracket)) {
      sizeExpr = parseExpr();
    }

    lexer.expectToken(TokenType.CloseBracket);

    if (lexer.acceptToken(TokenType.OpenBrace)) {
      while (!lexer.acceptToken(TokenType.CloseBrace)) {
        if (initExprCount >= initExprs.length) {
          Expr[] newArr = new Expr[initExprs.length * 2];
          System.arraycopy(initExprs, 0, newArr, 0, initExprs.length);
          initExprs = newArr;
        }

        initExprs[initExprCount++] = parseExpr();

        if (!lexer.peekToken(TokenType.CloseBrace)) {
          lexer.expectToken(TokenType.Comma);
        }
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

  private BlockExpr parseBlockExpr() {
    lexer.expectToken(TokenType.OpenBrace);

    int statementCount = 0;
    Statement[] statements = new Statement[1];

    while (!lexer.acceptToken(TokenType.CloseBrace)) {
      if (statementCount >= statements.length) {
        Statement[] newArr = new Statement[statements.length * 2];
        System.arraycopy(statements, 0, newArr, 0, statements.length);
        statements = newArr;
      }

      statements[statementCount++] = parseStatement();
    }

    return new BlockExpr(statementCount, statements);
  }

  private AbstractTypeRef parseTypeRef() {
    if (lexer.peekToken(TokenType.OpenBracket)) {
      return parseArrayTypeRef();
    } else {
      return parseNonArrayTypeRef();
    }
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
