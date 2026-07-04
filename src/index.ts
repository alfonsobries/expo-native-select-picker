import { SelectPickerOptions } from './ExpoIosSelectPicker.types';
import ExpoIosSelectPickerModule from './ExpoIosSelectPickerModule';

export type { SelectOption, SelectPickerOptions } from './ExpoIosSelectPicker.types';

/**
 * Opens the native iOS option list (the settings-style sheet: searchable,
 * A–Z sections with the side index, checkmark on the selected row) and
 * resolves with the picked value, or null when dismissed without picking.
 * Resolves null on platforms without the native implementation.
 */
export async function pickOption(options: SelectPickerOptions): Promise<string | null> {
  if (!ExpoIosSelectPickerModule) {
    return null;
  }
  return ExpoIosSelectPickerModule.presentAsync(options);
}
