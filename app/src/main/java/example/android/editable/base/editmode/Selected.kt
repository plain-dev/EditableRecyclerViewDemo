package example.android.editable.base.editmode

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 选择项实体
 */
@Parcelize
open class Selected : Parcelable{

    open var isSelected: Boolean = false

}