import { SelectPickerOptions } from './ExpoNativeSelect.types';
import ExpoNativeSelectModule from './ExpoNativeSelectModule';

export type {
  SelectOption,
  SelectPickerColors,
  SelectPickerOptions,
} from './ExpoNativeSelect.types';

/**
 * Opens the native iOS option list (the settings-style sheet: searchable,
 * A–Z sections with the side index, checkmark on the selected row) and
 * resolves with the picked value, or null when dismissed without picking.
 * Resolves null on platforms without the native implementation.
 */
export async function pickOption(options: SelectPickerOptions): Promise<string | null> {
  if (!ExpoNativeSelectModule) {
    return null;
  }
  return ExpoNativeSelectModule.presentAsync(options);
}
