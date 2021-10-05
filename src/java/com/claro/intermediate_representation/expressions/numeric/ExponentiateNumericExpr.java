package com.claro.intermediate_representation.expressions.numeric;

import com.claro.compiler_backends.interpreted.ScopedHeap;
import com.claro.intermediate_representation.expressions.Expr;
import com.claro.intermediate_representation.types.ClaroTypeException;
import com.claro.intermediate_representation.types.Type;
import com.claro.intermediate_representation.types.Types;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class ExponentiateNumericExpr extends NumericExpr {

  private static final ImmutableSet<Type> SUPPORTED_EXPONENTIATE_OPERAND_TYPES =
      ImmutableSet.of(Types.INTEGER, Types.FLOAT);

  // TODO(steving) This should only accept other NumericExpr args. Need to update the grammar.
  public ExponentiateNumericExpr(Expr lhs, Expr rhs) {
    super(ImmutableList.of(lhs, rhs));
  }

  @Override
  public Type getValidatedExprType(ScopedHeap scopedHeap) throws ClaroTypeException {
    assertSupportedExprType(
        ((Expr) this.getChildren().get(0)).getValidatedExprType(scopedHeap), SUPPORTED_EXPONENTIATE_OPERAND_TYPES);
    assertSupportedExprType(
        ((Expr) this.getChildren().get(1)).getValidatedExprType(scopedHeap), SUPPORTED_EXPONENTIATE_OPERAND_TYPES);

    return Types.FLOAT;
  }

  @Override
  public StringBuilder generateJavaSourceBodyOutput(ScopedHeap scopedHeap) {
    return new StringBuilder(
        String.format(
            "Math.pow(%s, %s)",
            ((Expr) this.getChildren().get(0)).generateJavaSourceBodyOutput(scopedHeap),
            ((Expr) this.getChildren().get(1)).generateJavaSourceBodyOutput(scopedHeap)
        )
    );
  }

  @Override
  public Object generateInterpretedOutput(ScopedHeap scopedHeap) {
    return Math.pow(
        (double) this.getChildren().get(0).generateInterpretedOutput(scopedHeap),
        (double) this.getChildren().get(1).generateInterpretedOutput(scopedHeap)
    );

  }
}