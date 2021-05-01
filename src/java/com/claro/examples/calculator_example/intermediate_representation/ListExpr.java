package com.claro.examples.calculator_example.intermediate_representation;

import com.claro.examples.calculator_example.compiler_backends.interpreted.ScopedHeap;
import com.claro.examples.calculator_example.intermediate_representation.types.ClaroTypeException;
import com.claro.examples.calculator_example.intermediate_representation.types.Type;
import com.claro.examples.calculator_example.intermediate_representation.types.Types;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListExpr extends Expr {

  private Optional<Type> emptyListValueType;
  private final ImmutableList<Expr> initializerArgExprsList;

  public ListExpr(ImmutableList<Expr> listInitializerArgsList) {
    super(ImmutableList.of());
    this.emptyListValueType = Optional.empty();
    this.initializerArgExprsList = listInitializerArgsList;
  }

  // TODO(steving) Drop this constructor option. We need the empty list type to be set. Use constructor below.
  public ListExpr() {
    super(ImmutableList.of());
    this.emptyListValueType = Optional.of(Types.UNDECIDED);
    this.initializerArgExprsList = ImmutableList.of();
  }

  public ListExpr(Type emptyListValueType) {
    super(ImmutableList.of());
    this.emptyListValueType = Optional.of(emptyListValueType);
    this.initializerArgExprsList = ImmutableList.of();
  }

  @Override
  protected Type getValidatedExprType(ScopedHeap scopedHeap) throws ClaroTypeException {
    Type listType;
    if (this.initializerArgExprsList.isEmpty()) {
      // The type of this empty list is known simply by the type that it was asserted to be within the statement context.
      listType = emptyListValueType.get();
    } else {
      Type listValuesType = ((Expr) this.initializerArgExprsList.get(0)).getValidatedExprType(scopedHeap);
      // Need to assert that all values in the list are of the same type.
      for (Node initialListValue : this.initializerArgExprsList) {
        ((Expr) initialListValue).assertExpectedExprType(scopedHeap, listValuesType);
      }
      listType = Types.ListType.forValueType(listValuesType);
    }
    return listType;
  }

  @Override
  protected void assertExpectedExprType(ScopedHeap scopedHeap, Type expectedExprType) throws ClaroTypeException {
    if (initializerArgExprsList.isEmpty()) {
      // For empty lists, the type assertion is actually used as the injection of context of this list's assumed type.
      this.emptyListValueType = Optional.of(expectedExprType);
    } else {
      super.assertExpectedExprType(scopedHeap, expectedExprType);
    }
  }

  @Override
  protected StringBuilder generateJavaSourceOutput(ScopedHeap scopedHeap) {
    // Simply for parity with the interpreted implementation, this is how we'll get this ArrayList.
    String listFormatString = "initializeList(%s)";
    String formatArg;
    if (initializerArgExprsList.isEmpty()) {
      formatArg = "";
    } else {
      formatArg =
          this.initializerArgExprsList.stream()
              .map(expr -> expr.generateJavaSourceOutput(scopedHeap))
              .collect(Collectors.joining(", ")
              );
    }
    return new StringBuilder(String.format(listFormatString, formatArg));
  }

  @Override
  protected Object generateInterpretedOutput(ScopedHeap scopedHeap) {
    return this.initializerArgExprsList.stream()
        .map(expr -> expr.generateInterpretedOutput(scopedHeap))
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
