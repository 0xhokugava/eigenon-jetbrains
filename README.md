# Eigenon Quantum Toolkit for JetBrains

A JetBrains IDE plugin for developing, running, visualizing, analyzing, and debugging quantum circuits with Eigenon.

The plugin integrates OpenQASM editing, source-located diagnostics, circuit execution, state inspection, circuit visualization, optimization tools, and interoperability workflows directly into JetBrains IDEs.

## Planned capabilities

- OpenQASM 2 and OpenQASM 3 language support
- Syntax highlighting, completion, formatting, and diagnostics
- Run configurations powered by the Eigenon simulator
- Circuit diagrams and operation statistics
- State-vector, probability, and measurement inspection
- Step-by-step circuit execution
- Optimization previews with before/after comparisons
- Rust Circuit API and OpenQASM conversion
- Qiskit and additional framework interoperability

> [!IMPORTANT]
> This project is in early development and does not yet provide a published JetBrains plugin.

## OpenQASM editor support

The plugin recognizes `.qasm` files and provides lexical syntax highlighting for OpenQASM 2.0 and OpenQASM 3.x. Keywords, types, declarations, built-in gates and constants, identifiers, literals, comments, operators, and punctuation use IDE-configurable colors under **Editor | Color Scheme | OpenQASM**.

This initial language layer works without the Eigenon executable. It does not yet provide structured parsing, semantic validation, completion, navigation, formatting, refactoring, or execution. OpenQASM 3 support is lexical and does not imply that the Eigenon runtime can import every OpenQASM 3 construct.

## Development

The project scaffold follows the official [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template) and uses the IntelliJ Platform Gradle Plugin 2.x.

Prerequisites:

- JDK 21

Common tasks:

```shell
./gradlew check
./gradlew buildPlugin
./gradlew verifyPlugin
./gradlew runIde
```

The built plugin ZIP is written to `build/distributions`. CI runs the build, platform tests, and Plugin Verifier for pushes to `master` and for pull requests.

## Related project

The plugin is powered by the [Eigenon](https://github.com/0xhokugava/eigenon).
