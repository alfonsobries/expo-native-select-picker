import { registerWebModule, NativeModule } from 'expo';

class ExpoIosSelectPickerModule extends NativeModule<{}> {
  async setValueAsync(value: string): Promise<void> {}
}

export default registerWebModule(ExpoIosSelectPickerModule, 'ExpoIosSelectPickerModule');
