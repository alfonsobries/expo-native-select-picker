import { SelectPickerOptions } from './ExpoNativeSelectPicker.types';
import ExpoNativeSelectPickerModule from './ExpoNativeSelectPickerModule';

export type {
  SelectOption,
  SelectPickerColors,
  SelectPickerOptions,
} from './ExpoNativeSelectPicker.types';

/**
 * Opens the native iOS option list (the settings-style sheet: searchable,
 * A–Z sections with the side index, checkmark on the selected row) and
 * resolves with the picked value, or null when dismissed without picking.
 * Resolves null on platforms without the native implementation.
 */
export async function pickOption(options: SelectPickerOptions): Promise<string | null> {
  if (!ExpoNativeSelectPickerModule) {
    return null;
  }
  return ExpoNativeSelectPickerModule.presentAsync(options);
}
