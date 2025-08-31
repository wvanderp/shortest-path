# Shortest Path

Draws the shortest path to a chosen destination on the map (right click a spot on the world map or shift right click a tile to use).

![illustration](https://user-images.githubusercontent.com/53493631/154380329-e1cacdce-a589-4ac3-b6d8-d0dc19f88b2a.png)

## Issues, bugs, suggestions and help
|Problem|Link|
|:--|:-:|
|**🐛 Bug report**<br>Report an issue encountered while using the plugin, or take a look at [already reported bugs](../../issues?q=is%3Aopen+is%3Aissue+label%3Abug).|[![issue](https://github.com/user-attachments/assets/983e048d-75c6-4fb8-9dd4-accbdc4588c0)](../../issues/new?assignees=&labels=bug&projects=&template=bug_report.md&title=)|
|**💡 Feature request**<br>Request a new feature or suggestion, or take a look at [already reported enhancements](../../issues?q=is%3Aopen+is%3Aissue+label%3Aenhancement).|[![feature](https://github.com/user-attachments/assets/983e048d-75c6-4fb8-9dd4-accbdc4588c0)](../../issues/new?assignees=&labels=enhancement&projects=&template=feature_request.md&title=)|
|**💬 Discord server**<br>Found a bug, need help with using the plugin, or just want to talk about it with other people?|[![discord](https://github.com/user-attachments/assets/db4d6bfd-9529-4d94-b03d-6c3fd69f855a)](https://discord.gg/uX47xg8u3M)|

## Features, examples and options
Read the [plugin wiki](../../wiki) for info about features, examples and plugin options.

## Code Style & Formatting

Canonical formatter: `config/eclipse-java-formatter.xml` (GoogleStyle).

Editors:
- VS Code: uses Red Hat Java extension configured in `.vscode/settings.json` (format on save enabled).
- IntelliJ IDEA: Import via Settings → Editor → Code Style → Java → Scheme (Project) → … → Import Scheme → Eclipse XML → select `config/eclipse-java-formatter.xml` (profile `GoogleStyle`).

CI:
- Enforced with Spotless (`./gradlew --no-daemon spotlessCheck`). The CI workflow `format-check.yml` runs on every push and PR.

Local usage:
- Check: `./gradlew spotlessCheck`
- Apply fixes: `./gradlew spotlessApply`

Whitespace basics enforced through `.editorconfig` (indent = 4 spaces, LF line endings, UTF-8, trailing whitespace trimmed, final newline ensured).

Note: This PR only adds formatting infrastructure and does not bulk reformat existing Java sources to keep the diff focused. A follow-up command to apply formatting is `./gradlew spotlessApply`.
