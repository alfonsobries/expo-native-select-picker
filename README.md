# expo-ios-select-picker

[![npm version](https://img.shields.io/npm/v/expo-ios-select-picker.svg)](https://www.npmjs.com/package/expo-ios-select-picker)
[![CI](https://github.com/alfonsobries/expo-ios-select-picker/actions/workflows/ci.yml/badge.svg)](https://github.com/alfonsobries/expo-ios-select-picker/actions/workflows/ci.yml)
[![license](https://img.shields.io/npm/l/expo-ios-select-picker.svg)](./LICENSE)

Native iOS select picker for Expo and React Native: the **settings-style option list** — the same UIKit table iOS uses to pick your region — with a native search bar, A–Z sections with the side index, and a checkmark on the selected row. Call it from any trigger and it resolves with the picked value, like a `<select>` should.

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/countries-dark.png">
    <img src="docs/countries-light.png" alt="Native option list with search, A–Z sections, and the side index" width="360">
  </picture>
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/short-list-dark.png">
    <img src="docs/short-list-light.png" alt="Plain short option list with a checkmark on the selected row" width="360">
  </picture>
</p>

- 🍎 **Real native UI** — `UITableViewController` + `UISearchController`, exactly what Settings uses. Not a re-implementation.
- 🔍 **Native search** — diacritic-insensitive ("mexico" finds "México"), disable it for short lists.
- 🔤 **A–Z sections + side index** — automatic for long lists, controllable via `grouped`.
- ✅ **Checkmark** on the current value; emoji in labels (flags!) work and don't break grouping.
- 🎯 **Headless** — trigger from any button or field; there's no UI to style.
- 🪶 **Zero dependencies.**

## Requirements

- iOS 16+
- Expo SDK 52+ with a [development build](https://docs.expo.dev/develop/development-builds/introduction/) or a bare React Native app with Expo Modules — this package includes native code, so it does **not** run in Expo Go.

On Android and web the helper resolves `null` so cross-platform code doesn't need guards.

## Installation

```sh
npx expo install expo-ios-select-picker
```

Then rebuild your development build (`npx expo run:ios` or an EAS build). If you manage OTA updates with a fixed `runtimeVersion`, adding this package is a native change — bump it.

## Usage

```tsx
import { pickOption } from 'expo-ios-select-picker';

const value = await pickOption({
  title: 'Country',
  options: [
    { value: 'mx', label: '🇲🇽 México' },
    { value: 'es', label: '🇪🇸 España' },
    // …
  ],
  selectedValue: 'mx',
  searchPlaceholder: 'Search country',
});

if (value !== null) {
  setCountry(value); // null when dismissed without picking
}
```

### Short lists

```tsx
const priority = await pickOption({
  title: 'Priority',
  options: [
    { value: 'low', label: 'Low' },
    { value: 'medium', label: 'Medium' },
    { value: 'high', label: 'High' },
  ],
  selectedValue: 'medium',
  searchable: false,
});
```

### As a form field

The picker is headless, so wrapping it in a field component of your design system takes a few lines:

```tsx
function Select({ label, value, onChange, options }) {
  return (
    <Pressable
      onPress={async () => {
        const picked = await pickOption({ title: label, options, selectedValue: value });
        if (picked !== null) {
          onChange(picked);
        }
      }}
    >
      <Text>{options.find((option) => option.value === value)?.label ?? 'Select…'}</Text>
    </Pressable>
  );
}
```

## API

### `pickOption(options): Promise<string | null>`

Presents the option list as a native sheet and resolves with the picked `value`, or `null` when dismissed without picking (close button or swipe).

### `SelectPickerOptions`

| Option              | Type             | Default   | Description                                                                     |
| ------------------- | ---------------- | --------- | ------------------------------------------------------------------------------- |
| `options`           | `SelectOption[]` | —         | The list to pick from: `{ value: string, label: string }`. Emoji in labels work. |
| `title`             | `string`         | —         | Navigation-bar title of the sheet.                                               |
| `selectedValue`     | `string`         | —         | Value shown with a checkmark.                                                    |
| `searchable`        | `boolean`        | `true`    | Native search bar; matching is case- and diacritic-insensitive.                  |
| `searchPlaceholder` | `string`         | system    | Placeholder of the search bar.                                                   |
| `grouped`           | `boolean`        | automatic | A–Z sections with the side index. Automatic turns it on at 30+ options.          |

Grouping keys on the first *letter* of each label, so a leading flag or emoji doesn't affect it; labels that start with no letter land under `#`.

## Example

The [`example`](./example) app demonstrates the searchable country list, a short list without search, and preselected values:

```sh
cd example
npx expo run:ios
```

## Contributing

Issues and PRs are welcome. Commits follow [Conventional Commits](https://www.conventionalcommits.org) — releases and the changelog are generated from them.

```sh
npm install
npm run lint
npm test
npm run build
```

## License

[MIT](./LICENSE) © [Alfonso Bribiesca](https://alfonsobries.com)
