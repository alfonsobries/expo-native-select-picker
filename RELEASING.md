# Releasing

Releases are fully automated with [semantic-release](https://github.com/semantic-release/semantic-release): every push to `main` runs CI and, when there are releasable commits, publishes to npm, tags, updates `CHANGELOG.md`, and creates a GitHub release.

## How versions are decided

The version bump comes from [Conventional Commit](https://www.conventionalcommits.org) messages since the last release:

| Commit                                          | Release |
| ----------------------------------------------- | ------- |
| `fix: …`                                        | patch   |
| `feat: …`                                       | minor   |
| `feat!: …` or a `BREAKING CHANGE:` footer       | major   |
| `chore: …`, `docs: …`, `refactor: …`, `test: …` | none    |

There is nothing to do manually — no `npm version`, no tags, no publish.

## One-time setup

The release workflow needs an npm automation token in the repository secrets:

1. Create a token at [npmjs.com → Access Tokens](https://www.npmjs.com/settings/alfonsobries/tokens).
   Until the first release exists, scope it to **all packages** — a granular token cannot be
   pointed at `expo-native-select-picker` before the package is published. Enable **bypass 2FA on publish**.
2. Add it as the `NPM_TOKEN` secret: `gh secret set NPM_TOKEN --repo alfonsobries/expo-native-select-picker`.
3. After the first release you can swap it for a token scoped to this package alone.

`GITHUB_TOKEN` is provided by Actions automatically.
