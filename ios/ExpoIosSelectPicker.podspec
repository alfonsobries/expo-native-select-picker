Pod::Spec.new do |s|
  s.name           = 'ExpoIosSelectPicker'
  s.version        = '1.0.0'
  s.summary        = 'Native iOS select picker: the settings-style option list'
  s.description    = 'Presents the system settings-style option list (native search, A-Z sections with the side index, checkmark on the selected row) and resolves with the picked value.'
  s.author         = 'Alfonso Bribiesca'
  s.homepage       = 'https://github.com/alfonsobries/expo-ios-select-picker'
  s.platforms      = {
    :ios => '16.4',
    :tvos => '16.4'
  }
  s.source         = { git: 'https://github.com/alfonsobries/expo-ios-select-picker.git' }
  s.static_framework = true

  s.dependency 'ExpoModulesCore'

  # Swift/Objective-C compatibility
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
  }

  s.source_files = "**/*.{h,m,mm,swift,hpp,cpp}"
end
