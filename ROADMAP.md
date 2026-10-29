# Roadmap

This roadmap describes the intended development direction for Eigenon Quantum Toolkit. It is directional rather than a release commitment: priorities and version boundaries may change as the OpenQASM language layer and the Eigenon CLI evolve.

## 0.1 — Marketplace foundation

The initial release establishes the plugin and its distribution pipeline.

- OpenQASM 2.0 and OpenQASM 3.x `.qasm` file recognition
- Lexical syntax highlighting and configurable editor colors
- Eigenon executable discovery and local path configuration
- Plugin signing, verification, CI, and JetBrains Marketplace metadata
- Project privacy policy and release documentation

## 0.2 — Structured language model

Replace the current token-only language layer with a structured PSI model.

- Structured parsing for the supported OpenQASM 2.0 and 3.x syntax
- Syntax diagnostics with recovery during incomplete edits
- File-local symbol resolution
- Semantic diagnostics for unresolved and duplicate declarations
- Statically detectable register index validation

## 0.3 — IDE assistance

Build language-aware editing features on top of parsing and resolution.

- Dialect-aware keyword, gate, constant, and symbol completion
- Go to Declaration and Find Usages
- OpenQASM code formatting
- Comment toggling, brace matching, and delimiter pairing
- Stable behavior for incomplete and temporarily invalid files

## 0.4 — Project-wide intelligence

Extend language support beyond a single file.

- Resolution of local `include` targets
- Project indexing for OpenQASM declarations
- Cross-file navigation and usage search
- Rename refactoring for supported symbols
- Documentation and parameter information

## 0.5 — Eigenon integration

Introduce a stable boundary between the IDE plugin and the Eigenon CLI.

- CLI version and capability detection
- A documented, machine-readable invocation and output contract
- Background process execution with cancellation and timeouts
- Structured presentation of CLI errors and diagnostics
- Compatibility handling for unsupported CLI versions

This milestone depends on a stable OpenQASM import or validation contract in the main [Eigenon](https://github.com/0xhokugava/eigenon) project.

## 0.6 — Run workflow

Allow supported OpenQASM programs to be executed from JetBrains IDEs.

- OpenQASM run configurations
- Run and stop actions for the active file
- Configurable execution parameters
- IDE console output and execution status
- Safe handling of unsaved files and invalid configurations

## 0.7 — Quantum tooling

Present Eigenon execution results in IDE-native views.

- Circuit visualization
- Measurement results and histograms
- Export of supported result formats
- Run history and reproducibility metadata
- Initial debugging foundations where supported by Eigenon

## 1.0 — Stable release

Stabilize the complete editing and execution workflow.

- Performance and memory profiling on representative projects
- Compatibility verification across supported JetBrains IDEs
- End-to-end tests for editing, configuration, and execution
- Accessibility and user-experience review
- Complete user and contributor documentation
- A documented compatibility and deprecation policy

## Dependency order

The major implementation dependencies are:

```text
Lexer
  -> structured parser
    -> symbol resolution and diagnostics
      -> completion and navigation
        -> formatting and refactoring
          -> cross-file support
            -> Eigenon CLI integration
              -> run workflow
                -> visualization and debugging
```

