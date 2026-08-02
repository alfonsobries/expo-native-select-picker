package expo.modules.nativeselect

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/**
 * A magnifier drawn as strokes rather than a bitmap.
 *
 * The platform's `ic_menu_search` is a chunky leftover from Holo and sits
 * badly next to a modern field; a circle and a handle at the current
 * stroke weight look right at any density and tint cleanly.
 */
internal class SearchIconDrawable(
  private val size: Int,
  color: Int
) : Drawable() {

  private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeWidth = size / 11f
    strokeCap = Paint.Cap.ROUND
    this.color = color
  }

  override fun draw(canvas: Canvas) {
    val bounds = bounds
    val radius = size * 0.28f
    val centerX = bounds.left + size * 0.42f
    val centerY = bounds.top + size * 0.42f

    canvas.drawCircle(centerX, centerY, radius, paint)
    canvas.drawLine(
      centerX + radius * 0.75f,
      centerY + radius * 0.75f,
      bounds.left + size * 0.86f,
      bounds.top + size * 0.86f,
      paint
    )
  }

  override fun getIntrinsicWidth() = size

  override fun getIntrinsicHeight() = size

  override fun setAlpha(alpha: Int) {
    paint.alpha = alpha
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    paint.colorFilter = colorFilter
  }

  @Deprecated("Deprecated in Drawable", ReplaceWith("PixelFormat.TRANSLUCENT"))
  override fun getOpacity() = PixelFormat.TRANSLUCENT
}
