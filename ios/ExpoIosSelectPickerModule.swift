import ExpoModulesCore

public class ExpoIosSelectPickerModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoIosSelectPicker")

    AsyncFunction("setValueAsync") { (value: String) in
    }
  }
}
