# Contributing to Eigenon Quantum Toolkit for JetBrains

Thank you for helping improve the project. Contributions of code, tests, documentation, bug reports, and design feedback are welcome.

## Before you start

- Search existing issues and pull requests to avoid duplicating work.
- Open an issue before starting a substantial feature or architectural change.
- Report security vulnerabilities privately according to [SECURITY.md](SECURITY.md), not in a public issue.
- Follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Development setup

The project requires JDK 21 and uses the Gradle Wrapper.

```shell
./gradlew check
./gradlew buildPlugin
./gradlew verifyPlugin
```

Use `./gradlew runIde` to start a development IDE with the plugin installed.

## Making a change

1. Fork the repository and create a focused branch from `master`.
2. Keep changes scoped to one concern and avoid unrelated formatting changes.
3. Add or update tests for behavior changes.
4. Update documentation and `CHANGELOG.md` when the change is user-visible.
5. Run the relevant Gradle checks locally.
6. Open a pull request using the repository template.

Commit messages should be concise and imperative. Conventional Commit prefixes such as `feat:`, `fix:`, `test:`, and `docs:` are encouraged.

## Pull request expectations

A pull request should:

- explain the problem and the chosen solution;
- link related issues;
- include verification steps and results;
- pass all required CI checks;
- contain no credentials, private data, generated build output, or unrelated changes;
- remain open to review and requested revisions.

By submitting a contribution, you agree that it is licensed under the repository's [GNU General Public License v3.0](LICENSE).
