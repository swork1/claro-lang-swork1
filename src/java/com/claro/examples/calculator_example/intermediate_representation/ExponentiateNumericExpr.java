package com.claro.examples.calculator_example.intermediate_representation;

import com.google.common.collect.ImmutableList;

public class ExponentiateNumericExpr extends NumericExpr {

  // TODO(steving) This should only accept other NumericExpr args. Need to update the grammar.
  public ExponentiateNumericExpr(Expr lhs, Expr rhs) {
    super(ImmutableList.of(lhs, rhs));
  }

  @Override
  protected StringBuilder generateJavaSourceOutput() {
    return new StringBuilder(
        String.format(
            "Math.pow(%s, %s)",
            this.getChildren().get(0).generateJavaSourceOutput(),
            this.getChildren().get(1).generateJavaSourceOutput()
        )
    );
  }
}