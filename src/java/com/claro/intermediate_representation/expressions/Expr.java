package com.claro.intermediate_representation.expressions;

import com.claro.compiler_backends.interpreted.ScopedHeap;
import com.claro.intermediate_representation.Node;
import com.claro.intermediate_representation.types.BaseType;
import com.claro.intermediate_representation.types.ClaroTypeException;
import com.claro.intermediate_representation.types.ConcreteTypes;
import com.claro.intermediate_representation.types.Type;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Expr extends Node {
  protected boolean acceptUndecided = false;

  // Use these fields to provide more richer error messaging rather than simply throwing Exceptions.
  public static final Stack<Consumer<String>> typeErrorsFound = new Stack<>();
  public final Supplier<String> currentLine;
  public final int currentLineNumber;
  public final int startCol;
  public final int endCol;

  public Expr(ImmutableList<Node> children, Supplier<String> currentLine, int currentLineNumber, int startCol, int endCol) {
    super(children);
    this.currentLine = currentLine;
    this.currentLineNumber = currentLineNumber;
    this.startCol = startCol;
    this.endCol = endCol;
  }

  // Exprs are Language constructs that have associated values, which mean that they are of a certain Type. All Expr
  // consumers in the Compiler *must* assert that the type is one that's expected so that we can report on type
  // mismatches at compile-time instead of failing at runtime.

  // Impls must return the validated type of the curr Expr. "Validated" in this context means that if sub-expressions
  // must be a certain type for the Expr to be semantically meaningful under a certain type interpretation, then it must
  // be asserted that all sub-expressions do in fact have that corresponding type. E.g. in the Expr log_2(...) the arg
  // expr must be compatible with type Double (either it's a Double, subclass of Double, or it's an Integer which can be
  // automatically upcasted to Double) and then this method would return Double for the overall log Expr, otherwise it
  // would throw a type exception.
  public abstract Type getValidatedExprType(ScopedHeap scopedHeap) throws ClaroTypeException;

  // Exprs should override this method if they need to do something fancier like supporting multiple contexts (e.g. an
  // int Expr should be able to just represent itself as a double Expr). In that case, this impl, should actually
  // modify internal state such that when generate*Output is called afterwards, it will produce the expected type.
  public void assertExpectedExprType(ScopedHeap scopedHeap, Type expectedExprType)
      throws ClaroTypeException {
    try {
      Type validatedExprType = this.getValidatedExprType(scopedHeap);
      this.assertNoUndecidedTypeLeak(validatedExprType, expectedExprType);

      if (!validatedExprType.equals(expectedExprType)) {
        throw new ClaroTypeException(validatedExprType, expectedExprType);
      }
    } catch (Exception e) {
      // I actually don't want to propagate Exceptions in these scenarios anymore because that will prevent us from
      // notifying users of as many errors as possible upfront.
      logTypeError(e);
    }
  }

  public final Type assertSupportedExprType(
      ScopedHeap scopedHeap, ImmutableSet<Type> supportedExprTypes) throws ClaroTypeException {
    // TODO(steving) Once this is no longer unnecessarily static, replace this logic with a call to this.assertNoUndecidedTypeLeak();
    try {
      Type validatedExprType = this.getValidatedExprType(scopedHeap);
      if (validatedExprType.equals(ConcreteTypes.UNDECIDED)) {
        throw ClaroTypeException.forUndecidedTypeLeak(supportedExprTypes);
      }

      if (!supportedExprTypes.contains(validatedExprType)) {
        throw new ClaroTypeException(validatedExprType, supportedExprTypes);
      }

      return validatedExprType;
    } catch (Exception e) {
      // I actually don't want to propagate Exceptions in these scenarios anymore because that will prevent us from
      // notifying users of as many errors as possible upfront.
      logTypeError(e);

      // We already logged an error so in order to move on and find other errors, let's just pretend that we were able
      // to correct the type.
      return supportedExprTypes.asList().get(0);
    }
  }

  public final void assertExpectedBaseType(ScopedHeap scopedHeap, BaseType expectedBaseType)
      throws ClaroTypeException {
    try {
      Type validatedExprType = this.getValidatedExprType(scopedHeap);
      this.assertNoUndecidedTypeLeak(validatedExprType, expectedBaseType);

      if (!validatedExprType.baseType().equals(expectedBaseType)) {
        throw new ClaroTypeException(validatedExprType, expectedBaseType);
      }
    } catch (Exception e) {
      // I actually don't want to propagate Exceptions in these scenarios anymore because that will prevent us from
      // notifying users of as many errors as possible upfront.
      logTypeError(e);
    }
  }

  public final BaseType assertSupportedExprBaseType(ScopedHeap scopedHeap, ImmutableSet<BaseType> supportedBaseTypes)
      throws ClaroTypeException {
    try {
      Type validatedExprType = this.getValidatedExprType(scopedHeap);
      this.assertNoUndecidedTypeLeak(validatedExprType, supportedBaseTypes);

      if (!supportedBaseTypes.contains(validatedExprType.baseType())) {
        throw new ClaroTypeException(validatedExprType, supportedBaseTypes);
      }

      return validatedExprType.baseType();
    } catch (Exception e) {
      // I actually don't want to propagate Exceptions in these scenarios anymore because that will prevent us from
      // notifying users of as many errors as possible upfront.
      logTypeError(e);

      // We already logged an error so in order to move on and find other errors, let's just pretend that we were able
      // to correct the type.
      return supportedBaseTypes.asList().get(0);
    }
  }

  public final void setAcceptUndecided(boolean acceptUndecided) {
    this.acceptUndecided = acceptUndecided;
  }

  // This method will be used by ALL assert*Type methods above to ensure that we're never leaking an UNDECIDED type
  // where we don't explicitly allow it.
  protected final <T> void assertNoUndecidedTypeLeak(
      Type exprType, T contextuallyExpectedType) throws ClaroTypeException {
    try {
      if (!this.acceptUndecided) {
        if (exprType.equals(ConcreteTypes.UNDECIDED)) {
          throw ClaroTypeException.forUndecidedTypeLeak(contextuallyExpectedType);
        }
      }
    } catch (ClaroTypeException e) {
      // I actually don't want to propagate Exceptions in these scenarios anymore because that will prevent us from
      // notifying users of as many errors as possible upfront.
      logTypeError(e);
    }
  }

  public void logTypeError(Exception e) {
    Expr.typeErrorsFound.push(
        (filename) -> {
          System.err.println(
              String.format(
                  "%s.claro:%s: %s",
                  filename,
                  this.currentLineNumber + 1,
                  e.getMessage()
              ));
          String currentLineString = this.currentLine.get();
          if (Character.isWhitespace(currentLineString.charAt(currentLineString.length() - 1))) {
            int trailingWhitespaceStart = currentLineString.length();
            while (Character.isWhitespace(currentLineString.charAt(--trailingWhitespaceStart))) {
              ; // This is just cute for the sake of it....barf...but I'm keeping it lol.
            }
            System.err.println(currentLineString.substring(0, trailingWhitespaceStart + 1));
          } else {
            System.err.println(currentLineString);
          }
          System.err.print(Strings.repeat(" ", this.startCol));
          System.err.println(Strings.repeat("^", this.endCol - this.startCol));
        }
    );
  }

  public final GeneratedJavaSource generateJavaSourceOutput(ScopedHeap scopedHeap) {
    return GeneratedJavaSource.forJavaSourceBody(generateJavaSourceBodyOutput(scopedHeap));
  }

  public abstract StringBuilder generateJavaSourceBodyOutput(ScopedHeap scopedHeap);
}
