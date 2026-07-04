import { registerWebModule, NativeModule } from 'expo';

import { SelectPickerOptions } from './ExpoIosSelectPicker.types';

// The native option list is iOS-only; on web the picker resolves to null.
class ExpoIosSelectPickerModule extends NativeModule<Record<string, never>> {
  async presentAsync(_options: SelectPickerOptions): Promise<string | null> {
    return null;
  }
}

export default registerWebModule(ExpoIosSelectPickerModule, 'ExpoIosSelectPickerModule');
