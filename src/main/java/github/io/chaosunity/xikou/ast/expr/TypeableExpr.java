package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.ast.types.ClassTypeRef;

/**
 * Expressions that are possibly resolved into actual types. The acutal represented type
 * must not be other than class type.
 */
public interface TypeableExpr {
    ClassTypeRef asTypeRef();
}
