package com.claro.intermediate_representation.expressions.term;

import com.claro.compiler_backends.interpreted.ScopedHeap;
import com.claro.intermediate_representation.types.Type;
import com.claro.intermediate_representation.types.Types;

import java.util.function.Supplier;

final public class IntegerTerm extends Term {
  private final Integer value;

  public IntegerTerm(Integer value, Supplier<String> currentLine, int currentLineNumber, int startCol, int endCol) {
    super(currentLine, currentLineNumber, startCol, endCol);
    this.value = value;
  }

  public int getValue() {
    return this.value;
  }

  @Override
  public Type getValidatedExprType(ScopedHeap unusedScopedHeap) {
    return Types.INTEGER;
  }

  @Override
  public StringBuilder generateJavaSourceBodyOutput(ScopedHeap scopedHeap) {
    return new StringBuilder().append(this.value);
  }

  @Override
  public Object generateInterpretedOutput(ScopedHeap scopedHeap) {
    return this.getValue();
  }
}
