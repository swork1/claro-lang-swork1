package com.claro.intermediate_representation.expressions.term;

import com.claro.compiler_backends.interpreted.ScopedHeap;
import com.claro.intermediate_representation.types.Type;
import com.google.common.base.Preconditions;

public class IdentifierReferenceTerm extends Term {

  private final String identifier;

  public IdentifierReferenceTerm(String identifier) {
    // Hold onto the relevant data for code-gen later.
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return identifier;
  }

  @Override
  public Type getValidatedExprType(ScopedHeap scopedHeap) {
    // Make sure we check this will actually be a valid reference before we allow it.
    Preconditions.checkState(
        scopedHeap.isIdentifierDeclared(this.identifier),
        "No variable <%s> within the current scope!",
        this.identifier
    );
    Preconditions.checkState(
        scopedHeap.isIdentifierInitialized(this.identifier),
        "Variable <%s> may not have been initialized!",
        this.identifier
    );
    scopedHeap.markIdentifierUsed(this.identifier);
    return scopedHeap.getValidatedIdentifierType(this.identifier);
  }

  @Override
  public StringBuilder generateJavaSourceBodyOutput(ScopedHeap scopedHeap) {
    scopedHeap.markIdentifierUsed(this.identifier);
    return new StringBuilder(this.identifier);
  }

  @Override
  public Object generateInterpretedOutput(ScopedHeap scopedHeap) {
    scopedHeap.markIdentifierUsed(this.identifier);
    return scopedHeap.getIdentifierValue(this.identifier);
  }
}