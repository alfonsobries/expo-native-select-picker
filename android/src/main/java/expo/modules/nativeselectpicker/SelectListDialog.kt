package expo.modules.nativeselectpicker

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.Normalizer
import java.util.Locale

/**
 * The Android answer to the settings-style option list: a close action, a
 * search field, A–Z sections with an index strip on the edge, and a check
 * on the current value.
 *
 * **The list is sized to what it holds.** A handful of options comes up as
 * a bottom sheet the height of its rows, with a grabber; a long one takes
 * the screen. Sending three options full-screen is ceremony, and pushing a
 * long list into a sheet leaves it scrolling inside a scroll.
 *
 * Chrome is drawn from theme attributes rather than a Material theme, so
 * the dialog inherits the host app's colors and dark mode without forcing
 * a theme onto the activity that hosts React Native.
 */
internal class SelectListDialog(
  private val activity: Activity,
  private val options: SelectPresentOptions,
  private val onFinish: (String?) -> Unit
) : Dialog(activity) {

  private companion object {
    const val AUTO_GROUP_THRESHOLD = 30
    /** Rows that still fit a sheet without it swallowing the screen. */
    const val SHEET_MAX_ROWS = 8
    const val TYPE_HEADER = 0
    const val TYPE_OPTION = 1
  }

  private sealed class Row {
    data class Header(val title: String) : Row()
    data class Option(val item: SelectItem) : Row()
  }

  private var finished = false
  private var searching = false
  private var rows: List<Row> = emptyList()
  private var indexTitles: List<String> = emptyList()

  private lateinit var list: RecyclerView
  private lateinit var indexStrip: LinearLayout
  private val adapter = RowAdapter()

  private val grouped: Boolean
    get() = options.grouped ?: (options.options.size >= AUTO_GROUP_THRESHOLD)

  /**
   * `auto` reads the list: a short one with nothing to search or index has
   * no use for a whole screen and comes up as a sheet the height of its own
   * rows. `sheet` and `fullScreen` say it outright.
   */
  private val presentsAsSheet: Boolean
    get() = when (options.presentation) {
      "sheet" -> true
      "fullScreen" -> false
      else -> !options.searchable && !grouped && options.options.size <= SHEET_MAX_ROWS
    }

  // The caller's design system wins; the platform theme is the fallback.
  private val labelColor by lazy {
    parseColor(options.colors?.label) ?: themeColor(android.R.attr.textColorPrimary, Color.BLACK)
  }
  private val mutedColor by lazy {
    parseColor(options.colors?.muted) ?: themeColor(android.R.attr.textColorSecondary, Color.GRAY)
  }
  private val accentColor by lazy {
    parseColor(options.colors?.accent) ?: themeColor(android.R.attr.colorAccent, labelColor)
  }
  private val surfaceColor by lazy {
    parseColor(options.colors?.background) ?: themeColor(android.R.attr.colorBackground, Color.WHITE)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    setContentView(buildContent())

    // Either way the window's own background and padding go: left in, the
    // app leaks around the edges and it reads as a sheet that failed to
    // open. The sheet paints its own rounded surface underneath.
    window?.apply {
      setBackgroundDrawable(
        if (presentsAsSheet) ColorDrawable(Color.TRANSPARENT) else ColorDrawable(surfaceColor)
      )
      decorView.setPadding(0, 0, 0, 0)
      // The window takes the screen either way. A sheet is a surface
      // anchored to the bottom *inside* it, not a shorter window: a window
      // that ends where its content ends stops above the gesture bar and
      // leaves a band of dimmed app under the sheet.
      setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT
      )
      setGravity(Gravity.TOP)
      // A dialog window does not go edge-to-edge on its own: without all
      // three the surface stops above the gesture bar and the system paints
      // its own bar over the gap.
      WindowCompat.setDecorFitsSystemWindows(this, false)
      addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
      navigationBarColor = Color.TRANSPARENT
      // Android draws a scrim over a transparent bar unless told not to,
      // and that scrim is what reads as a black band under the sheet.
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        isNavigationBarContrastEnforced = false
      }
    }
    setOnCancelListener { finish(null) }
    rebuild(null)
  }

  override fun onStop() {
    super.onStop()
    // Covers every other way out: back gesture, tapping outside, the
    // activity going away.
    finish(null)
  }

  // MARK: - Views

  private fun buildContent(): View {
    if (presentsAsSheet) {
      return buildSheet()
    }

    val root = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(surfaceColor)
      // Keeps the close button clear of the status bar and the list clear
      // of the gesture bar, on every device.
      ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(0, bars.top, 0, bars.bottom)
        insets
      }
    }

    root.addView(buildHeader())

    if (options.searchable) {
      root.addView(buildSearch())
    }

    root.addView(buildList(), LinearLayout.LayoutParams(MATCH, 0, 1f))

    return root
  }

  /**
   * The short form: a sheet the height of its rows, with the grabber that
   * says it can be dismissed and the same rows as the full-screen list.
   */
  private fun buildSheet(): View {
    // Tapping the app above the sheet dismisses it, the way every sheet
    // does; the sheet itself swallows its own touches.
    val scrim = FrameLayout(activity).apply {
      setOnClickListener {
        finish(null)
        dismiss()
      }
    }

    val sheet = LinearLayout(activity).apply {
      isClickable = true
      orientation = LinearLayout.VERTICAL
      background = GradientDrawable().apply {
        setColor(surfaceColor)
        val radius = dp(28).toFloat()
        cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
      }
      ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(0, dp(10), 0, bars.bottom + dp(8))
        insets
      }
    }

    sheet.addView(
      grabber(),
      LinearLayout.LayoutParams(dp(36), dp(4)).apply {
        gravity = Gravity.CENTER_HORIZONTAL
      }
    )

    options.title?.takeIf { it.isNotBlank() }?.let { title ->
      sheet.addView(
        TextView(activity).apply {
          text = title
          textSize = 13f
          setTextColor(mutedColor)
          setPadding(dp(20), dp(14), dp(20), dp(4))
          maxLines = 1
        },
        LinearLayout.LayoutParams(MATCH, WRAP)
      )
    }

    sheet.addView(buildList(), LinearLayout.LayoutParams(MATCH, WRAP))

    scrim.addView(
      sheet,
      FrameLayout.LayoutParams(MATCH, WRAP, Gravity.BOTTOM)
    )

    return scrim
  }

  private fun grabber(): View = View(activity).apply {
    background = GradientDrawable().apply {
      setColor(withAlpha(mutedColor, 0.4f))
      cornerRadius = dp(2).toFloat()
    }
  }

  private fun buildHeader(): View {
    val header = LinearLayout(activity).apply {
      orientation = LinearLayout.HORIZONTAL
      gravity = Gravity.CENTER_VERTICAL
      setPadding(dp(4), dp(4), dp(4), dp(4))
    }

    val close = TextView(activity).apply {
      text = "✕"
      textSize = 20f
      setTextColor(labelColor)
      gravity = Gravity.CENTER
      contentDescription = "Cerrar"
      background = borderlessRipple()
      setOnClickListener {
        finish(null)
        dismiss()
      }
    }
    header.addView(close, LinearLayout.LayoutParams(dp(48), dp(48)))

    val title = TextView(activity).apply {
      text = options.title ?: ""
      textSize = 20f
      setTextColor(labelColor)
      setPadding(dp(8), 0, dp(48), 0)
      maxLines = 1
    }
    header.addView(title, LinearLayout.LayoutParams(MATCH, WRAP))

    return header
  }

  private fun buildSearch(): View {
    val field = EditText(activity).apply {
      hint = options.searchPlaceholder ?: "Buscar"
      setSingleLine()
      imeOptions = EditorInfo.IME_ACTION_SEARCH
      setTextColor(labelColor)
      setHintTextColor(mutedColor)
      textSize = 16f
      background = null
      setPadding(dp(20), dp(12), dp(20), dp(12))
      compoundDrawablePadding = dp(12)
      setCompoundDrawablesRelativeWithIntrinsicBounds(
        searchIcon(), null, null, null
      )
      addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = rebuild(s?.toString())
        override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
        override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
      })
    }

    val wrapper = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      addView(field, LinearLayout.LayoutParams(MATCH, WRAP))
      addView(divider(), LinearLayout.LayoutParams(MATCH, 1))
    }

    return wrapper
  }

  private fun buildList(): View {
    val container = FrameLayout(activity)

    list = RecyclerView(activity).apply {
      layoutManager = LinearLayoutManager(activity)
      adapter = this@SelectListDialog.adapter
      isVerticalScrollBarEnabled = true
    }
    container.addView(
      list,
      FrameLayout.LayoutParams(MATCH, if (presentsAsSheet) WRAP else MATCH)
    )

    indexStrip = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      gravity = Gravity.CENTER
      setPadding(dp(8), dp(8), dp(8), dp(8))
      visibility = View.GONE
      setOnTouchListener { view, event ->
        when (event.action) {
          MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
            scrollToIndex(view as LinearLayout, event.y)
            true
          }
          else -> false
        }
      }
    }
    container.addView(
      indexStrip,
      FrameLayout.LayoutParams(WRAP, MATCH, Gravity.END or Gravity.CENTER_VERTICAL)
    )

    return container
  }

  // MARK: - Data

  private fun rebuild(query: String?) {
    val trimmed = query?.trim().orEmpty()
    searching = trimmed.isNotEmpty()

    val needle = fold(trimmed)
    val visible = if (searching) {
      options.options.filter { fold(it.label).contains(needle) }
    } else {
      options.options
    }

    val built = mutableListOf<Row>()
    val titles = mutableListOf<String>()

    if (grouped && !searching) {
      visible
        .groupBy { indexTitle(it.label) }
        .toSortedMap(compareBy { if (it == "#") "￿" else it })
        .forEach { (letter, items) ->
          titles += letter
          built += Row.Header(letter)
          built += items.map { Row.Option(it) }
        }
    } else {
      built += visible.map { Row.Option(it) }
    }

    rows = built
    indexTitles = titles
    adapter.notifyDataSetChanged()
    renderIndexStrip()
  }

  /**
   * First letter of the label ignoring diacritics and any leading symbols
   * (the flag in "🇲🇽 México"); anything that is not a letter lands in "#".
   */
  private fun indexTitle(label: String): String {
    val letter = fold(label).firstOrNull { it.isLetter() } ?: return "#"
    return letter.uppercaseChar().toString()
  }

  /** Lowercased and stripped of diacritics, so "mexico" finds "México". */
  private fun fold(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD)
      .replace(Regex("\\p{Mn}+"), "")
      .lowercase(Locale.getDefault())

  // MARK: - Index strip

  private fun renderIndexStrip() {
    indexStrip.removeAllViews()

    if (!grouped || searching || indexTitles.size <= 1) {
      indexStrip.visibility = View.GONE
      return
    }

    indexTitles.forEach { letter ->
      indexStrip.addView(
        TextView(activity).apply {
          text = letter
          textSize = 11f
          setTextColor(accentColor)
          gravity = Gravity.CENTER
        },
        LinearLayout.LayoutParams(dp(20), WRAP)
      )
    }
    indexStrip.visibility = View.VISIBLE
  }

  private fun scrollToIndex(strip: LinearLayout, y: Float) {
    if (strip.childCount == 0) {
      return
    }

    val slot = (strip.height.toFloat() / strip.childCount).coerceAtLeast(1f)
    val index = ((y - strip.paddingTop) / slot).toInt().coerceIn(0, strip.childCount - 1)
    val letter = indexTitles.getOrNull(index) ?: return
    val position = rows.indexOfFirst { it is Row.Header && it.title == letter }

    if (position >= 0) {
      (list.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
    }
  }

  // MARK: - Rows

  private inner class RowAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount() = rows.size

    override fun getItemViewType(position: Int) =
      if (rows[position] is Row.Header) TYPE_HEADER else TYPE_OPTION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      if (viewType == TYPE_HEADER) {
        val header = TextView(activity).apply {
          textSize = 13f
          setTextColor(mutedColor)
          setPadding(dp(20), dp(16), dp(20), dp(6))
          layoutParams = RecyclerView.LayoutParams(MATCH, WRAP)
        }
        return object : RecyclerView.ViewHolder(header) {}
      }

      val row = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        minimumHeight = dp(56)
        setPadding(dp(20), dp(12), dp(20), dp(12))
        background = ripple()
        layoutParams = RecyclerView.LayoutParams(MATCH, WRAP)
      }
      row.addView(
        TextView(activity).apply {
          id = View.generateViewId()
          textSize = 16f
          setTextColor(labelColor)
          tag = "label"
        },
        LinearLayout.LayoutParams(0, WRAP, 1f)
      )
      row.addView(
        TextView(activity).apply {
          text = "✓"
          textSize = 18f
          setTextColor(accentColor)
          tag = "check"
        },
        LinearLayout.LayoutParams(WRAP, WRAP)
      )
      return object : RecyclerView.ViewHolder(row) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      when (val row = rows[position]) {
        is Row.Header -> (holder.itemView as TextView).text = row.title
        is Row.Option -> {
          val container = holder.itemView as LinearLayout
          (container.findViewWithTag<TextView>("label")).text = row.item.label
          (container.findViewWithTag<TextView>("check")).visibility =
            if (row.item.value == options.selectedValue) View.VISIBLE else View.INVISIBLE
          container.setOnClickListener {
            finish(row.item.value)
            dismiss()
          }
        }
      }
    }
  }

  // MARK: - Result

  private fun finish(value: String?) {
    if (finished) {
      return
    }
    finished = true
    onFinish(value)
  }

  // MARK: - Helpers

  private fun divider() = View(activity).apply {
    setBackgroundColor((mutedColor and 0x00FFFFFF) or 0x33000000)
  }

  private fun searchIcon() = SearchIconDrawable(dp(20), mutedColor)

  private fun ripple() = attrDrawable(android.R.attr.selectableItemBackground)

  private fun borderlessRipple() =
    attrDrawable(android.R.attr.selectableItemBackgroundBorderless)

  private fun attrDrawable(attr: Int) = TypedValue().let { value ->
    activity.theme.resolveAttribute(attr, value, true)
    ContextCompat.getDrawable(activity, value.resourceId)
  }

  private fun withAlpha(color: Int, alpha: Float): Int =
    Color.argb((alpha * 255).toInt(), Color.red(color), Color.green(color), Color.blue(color))

  private fun parseColor(value: String?): Int? {
    val hex = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    return runCatching { Color.parseColor(hex) }.getOrNull()
  }

  private fun themeColor(attr: Int, fallback: Int): Int {
    val value = TypedValue()
    if (!activity.theme.resolveAttribute(attr, value, true)) {
      return fallback
    }
    return if (value.resourceId != 0) {
      ContextCompat.getColor(activity, value.resourceId)
    } else {
      value.data
    }
  }

  private fun dp(value: Int) =
    (value * activity.resources.displayMetrics.density).toInt()
}

private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
