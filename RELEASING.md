# Releasing

Releases are fully automated with [semantic-release](https://github.com/semantic-release/semantic-release): every push to `main` runs CI and, when there are releasable commits, publishes to npm, tags, updates `CHANGELOG.md`, and creates a GitHub release.

## How versions are decided

The version bump comes from [Conventional Commit](https://www.conventionalcommits.org) messages since the last release:

| Commit                                            | Release |
| ------------------------------------------------- | ------- |
| `fix: …`                                           | patch   |
| `feat: …`                                          | minor   |
| `feat!: …` or a `BREAKING CHANGE:` footer          | major   |
| `chore: …`, `docs: …`, `refactor: …`, `test: …`    | none    |

There is nothing to do manually — no `npm version`, no tags, no publish.

## One-time setup

The release workflow needs an npm automation token in the repository secrets:

1. Create a **granular access token** at [npmjs.com → Access Tokens](https://www.npmjs.com/settings/alfonsobries/tokens) with read/write access to the `expo-native-select-picker` package and **bypass 2FA on publish** enabled.
2. Add it as the `NPM_TOKEN` secret: `gh secret set NPM_TOKEN --repo alfonsobries/expo-native-select-picker`.

`GITHUB_TOKEN` is provided by Actions automatically.
