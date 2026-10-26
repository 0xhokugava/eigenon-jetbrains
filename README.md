# Eigenon Quantum Toolkit for JetBrains

A JetBrains IDE plugin for editing OpenQASM files and configuring the Eigenon executable.

## Current capabilities

- OpenQASM 2.0 and OpenQASM 3.x `.qasm` file recognition
- Lexical syntax highlighting for declarations, types, built-in gates, constants, identifiers, literals, comments, operators, and punctuation
- Configurable colors under **Editor | Color Scheme | OpenQASM**
- Automatic discovery of the `eigenon` executable from `PATH`
- Manual executable selection and validation under **Settings | Tools | Eigenon**

This initial release does not provide semantic validation, completion, formatting, run configurations, circuit execution, visualization, or debugging.

> [!IMPORTANT]
> This project is in early development and does not yet provide a published JetBrains plugin.

## Installation

### From JetBrains Marketplace

After the first release is approved, open **Settings | Plugins | Marketplace**, search for **Eigenon Quantum Toolkit**, and select **Install**.

### From a local build

Build the plugin distribution:

```shell
./gradlew buildPlugin
```

In the IDE, open **Settings | Plugins**, select the gear menu, choose **Install Plugin from Disk**, and select the ZIP from `build/distributions`.

## OpenQASM editor support

The plugin recognizes `.qasm` files and provides lexical syntax highlighting for OpenQASM 2.0 and OpenQASM 3.x. Keywords, types, declarations, built-in gates and constants, identifiers, literals, comments, operators, and punctuation use IDE-configurable colors under **Editor | Color Scheme | OpenQASM**.

This initial language layer works without the Eigenon executable. It does not yet provide structured parsing, semantic validation, completion, navigation, formatting, refactoring, or execution. OpenQASM 3 support is lexical and does not imply that the Eigenon runtime can import every OpenQASM 3 construct.

## Eigenon executable

The plugin discovers an `eigenon` executable from `PATH` by default. To use a specific installation, open **Settings | Tools | Eigenon** and select the executable manually. The configured path is stored only in the local IDE configuration and is not written to the project.

To install the executable from the Eigenon source repository:

```shell
git clone https://github.com/0xhokugava/eigenon.git
cd eigenon
cargo install --path .
```

OpenQASM editing works without the executable. The current plugin release validates and stores the executable location but does not yet run circuits from the IDE.

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

## Support and project links

- [Issue tracker](https://github.com/0xhokugava/eigenon-jetbrains/issues)
- [Security policy](SECURITY.md)
- [Contributing guide](CONTRIBUTING.md)
- [GNU General Public License v3.0](LICENSE)
