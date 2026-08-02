package expo.modules.nativeselect

import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record

class SelectItem : Record {
  @Field
  var value: String = ""

  @Field
  var label: String = ""
}

class SelectPresentOptions : Record {
  @Field
  var title: String? = null

  @Field
  var options: List<SelectItem> = emptyList()

  @Field
  var selectedValue: String? = null

  @Field
  var searchable: Boolean = true

  @Field
  var searchPlaceholder: String? = null

  /** A–Z sections with the side index. null = automatic (on for long lists). */
  @Field
  var grouped: Boolean? = null

  @Field
  var colors: SelectColors? = null
}

/** Hex colors from the caller's design system; null means "use the theme". */
class SelectColors : Record {
  @Field
  var background: String? = null

  @Field
  var label: String? = null

  @Field
  var muted: String? = null

  @Field
  var accent: String? = null
}

class ExpoNativeSelectModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoNativeSelect")

    AsyncFunction("presentAsync") { options: SelectPresentOptions, promise: Promise ->
      val activity = appContext.activityProvider?.currentActivity
        ?: throw Exceptions.MissingActivity()

      activity.runOnUiThread {
        SelectListDialog(activity, options) { value -> promise.resolve(value) }.show()
      }
    }
  }
}
