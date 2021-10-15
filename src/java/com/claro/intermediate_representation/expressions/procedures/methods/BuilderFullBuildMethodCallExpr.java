package com.claro.intermediate_representation.expressions.procedures.methods;

import com.claro.compiler_backends.interpreted.ScopedHeap;
import com.claro.intermediate_representation.expressions.Expr;
import com.claro.intermediate_representation.types.ClaroTypeException;
import com.claro.intermediate_representation.types.Type;
import com.claro.intermediate_representation.types.Types;
import com.claro.intermediate_representation.types.impls.user_defined_impls.ClaroUserDefinedTypeImplementation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

// TODO(steving) Deprecate this hack in favor of actually implementing methods on objects, making the Builder an object.
public class BuilderFullBuildMethodCallExpr extends Expr {
  private final BuilderMethodCallExpr delegateBuilderMethodCallExpr;

  public BuilderFullBuildMethodCallExpr(BuilderMethodCallExpr delegateBuilderMethodCallExpr) {
    super(ImmutableList.of());
    this.delegateBuilderMethodCallExpr = delegateBuilderMethodCallExpr;
  }

  @Override
  public Type getValidatedExprType(ScopedHeap scopedHeap) throws ClaroTypeException {
    Types.BuilderType builderType = (Types.BuilderType) delegateBuilderMethodCallExpr.getValidatedExprType(scopedHeap);
    if (!delegateBuilderMethodCallExpr.setFieldValues.keySet()
        .containsAll(builderType.getBuiltType().getFieldTypes().keySet())) {
      throw ClaroTypeException.forUnsetRequiredStructMember(
          builderType.getBuiltType(),
          ImmutableSet.copyOf(
              Sets.difference(
                  builderType.getBuiltType().getFieldTypes().keySet(),
                  delegateBuilderMethodCallExpr.setFieldValues.keySet()
              ))
      );
    }
    return builderType.getBuiltType();
  }

  @Override
  public StringBuilder generateJavaSourceBodyOutput(ScopedHeap scopedHeap) {
    return delegateBuilderMethodCallExpr.generateJavaSourceBodyOutput(scopedHeap).append(".build()");
  }

  @Override
  public Object generateInterpretedOutput(ScopedHeap scopedHeap) {
    try {
      return
          ((ClaroUserDefinedTypeImplementation.ClaroUserDefinedTypeImplementationBuilder<?>)
               delegateBuilderMethodCallExpr.generateInterpretedOutput(scopedHeap)).build();
    } catch (ClaroTypeException e) {
      throw new RuntimeException("Internal Compiler Error: This should've been caught at type-checking.", e);
    }
  }
}