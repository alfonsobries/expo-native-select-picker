import { NativeModule, requireOptionalNativeModule } from 'expo';

import { SelectPickerOptions } from './ExpoNativeSelectPicker.types';

declare class ExpoNativeSelectPickerModule extends NativeModule<Record<string, never>> {
  /**
   * Presents the native option list sheet; resolves with the picked value, or
   * null when dismissed without picking.
   */
  presentAsync(options: SelectPickerOptions): Promise<string | null>;
}

// Null on platforms without the native implementation (Android); the public
// helper degrades gracefully instead of throwing at import time.
export default requireOptionalNativeModule<ExpoNativeSelectPickerModule>('ExpoNativeSelectPicker');
