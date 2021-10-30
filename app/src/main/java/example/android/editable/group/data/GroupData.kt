package example.android.editable.group.data

import androidx.annotation.DrawableRes
import example.android.editable.base.SectionSelectEntity

class GroupData : SectionSelectEntity() {

    var storeName: String = ""

    @DrawableRes
    var productCover: Int = 0
    var productName: String = ""
    var productType: String = ""
    var productPrice: String = ""

    override val isHeader: Boolean
        get() = storeName.isNotEmpty()

}