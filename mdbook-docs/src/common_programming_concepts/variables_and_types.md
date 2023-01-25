# Variables & Primitive Types

Claro is a statically-compiled, strictly typed language. Practically speaking, this means that the type of all variables
must be statically determined upon declaration of the variable, and may never change thereafter.

Claro has a few builtin primitive types (representing generally small or low-level "value types" that are immutable to
the programmer). These values are generally cheap to allocate on the stack, and are passed as copies to other functions.
More are coming soon, but for now the supported set of primitives include: int, float, string, boolean. The example
below shows how you'd define variables to represent values of each type:

```
var i: int = 10; # Any whole number. 
var f: float = 1.15; # Any decimal number.
var s: string = "very first string"; # Any sequence of chars.
var b: boolean = true; # true or false.
```

`var` : Keyword introducing / declaring a new variable.

`b` : the name we chose for this particular var.

`:` : a syntactic divider between a variable's name and its type.

`boolean` : the type of the variable, which constrains the domain of values which this variable may hold. 