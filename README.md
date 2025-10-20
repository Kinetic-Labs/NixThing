# NixThing

NixThing is a simple interpreter for the Nix language, written in Java.

## Project Structure

The `nix-thing` module contains the core logic of the interpreter. It is divided into the following packages:

*   `com.github.kinetic.nixthing.ast`: This package contains the classes that represent the Abstract Syntax Tree (AST) of a Nix expression. Each class corresponds to a different type of expression, such as an integer, a string, or a function call.
*   `com.github.kinetic.nixthing.core`: This package contains the core components of the interpreter, including the lexer, parser, and evaluator.
*   `com.github.kinetic.nixthing.lang`: This package contains classes related to the language's runtime, such as closures and lazy evaluation.

## Building and Running

To build the project and run the demo, use the following command:

```
./gradlew runDemo
```
