import { registerWebModule, NativeModule } from 'expo';

import { SelectPickerOptions } from './ExpoNativeSelectPicker.types';

// The native option list is iOS-only; on web the picker resolves to null.
class ExpoNativeSelectPickerModule extends NativeModule<Record<string, never>> {
  async presentAsync(_options: SelectPickerOptions): Promise<string | null> {
    return null;
  }
}

export default registerWebModule(ExpoNativeSelectPickerModule, 'ExpoNativeSelectPickerModule');
