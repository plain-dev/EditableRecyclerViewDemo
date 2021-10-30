@file:Suppress(
        "unused",
        "UNCHECKED_CAST",
        "NOTHING_TO_INLINE"
)

package example.android.editable.ktx

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.*
import androidx.core.content.ContextCompat
import example.android.editable.GlobalApplication

/**
 * 快捷获取上下文
 */
val appContext: Context = GlobalApplication.context

/**
 * 快捷获取[Resources]
 */
val resources: Resources = appContext.resources

/**
 * 将一个[Int]转为DP单位[Int]
 *
 * 例如 `18.dp`
 */
val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

/**
 * 将一个[Int]转为PX单位[Int]
 *
 * 例如 `18.px`
 */
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * 将一个[Float]转为DP单位[Float]
 *
 * 例如 `18.5f.dp`
 */
val Float.dp: Float
    get() = this / Resources.getSystem().displayMetrics.density

/**
 * 将一个[Float]转为PX单位[Float]
 *
 * 例如 `18.5f.px`
 */
val Float.px: Float
    get() = this * Resources.getSystem().displayMetrics.density

/**
 * Create a [Drawable] from this drawable res.
 */
@JvmName("getDrawable")
inline fun @receiver:DrawableRes Int.obtainDrawable() = ContextCompat.getDrawable(appContext, this)

/**
 * Create a Color from this color res.
 */
@JvmName("getColor")
@ColorInt
inline fun @receiver:ColorRes Int.obtainColor() = ContextCompat.getColor(appContext, this)

/**
 * Create a [String] from this string res.
 */
@JvmName("getString")
inline fun @receiver:StringRes Int.obtainString() = resources.getString(this)

/**
 * Create a [String] from this string res.
 */
@JvmName("getString")
inline fun @receiver:StringRes Int.obtainString(vararg formatArgs: Any) = resources.getString(this, *formatArgs)

/**
 * Create a Dimension from this dimen res.
 */
@JvmName("getDimensionPixelOffset")
inline fun @receiver:DimenRes Int.obtainDimensionPixelOffset() = resources.getDimensionPixelOffset(this)

/**
 * Create a Dimension from this dimen res.
 */
@JvmName("getDimensionPixelSize")
inline fun @receiver:DimenRes Int.obtainDimensionPixelSize() = resources.getDimensionPixelSize(this)

/**
 * Create a Dimension from this dimen res.
 */
@JvmName("getDimension")
inline fun @receiver:DimenRes Int.obtainDimension() = resources.getDimension(this)