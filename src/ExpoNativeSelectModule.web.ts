import { registerWebModule, NativeModule } from 'expo';

import { SelectPickerOptions } from './ExpoNativeSelect.types';

// The native option list is iOS-only; on web the picker resolves to null.
class ExpoNativeSelectModule extends NativeModule<Record<string, never>> {
  async presentAsync(_options: SelectPickerOptions): Promise<string | null> {
    return null;
  }
}

export default registerWebModule(ExpoNativeSelectModule, 'ExpoNativeSelectModule');
