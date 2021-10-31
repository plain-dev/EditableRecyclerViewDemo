package example.android.editable.debug

import example.android.editable.R
import example.android.editable.group.data.GroupData
import example.android.editable.ktx.obtainString

object SimpleData {

    fun getSimpleGroupData(): MutableList<GroupData> {

        return mutableListOf<GroupData>().apply {
            (1..3).forEach { sectionIndex ->
                val groupId = "100${sectionIndex}"
                add(
                    GroupData().also { local ->
                        local.groupId = groupId
                        local.storeName = "${R.string.store_simple_name.obtainString()} - $sectionIndex"
                    }
                )
                (1..(1..3).random()).forEach { childIndex ->
                    add(
                        GroupData().also { local ->
                            local.groupId = groupId
                            local.productCover = if ((0..2).random() % 2 == 0)
                                R.drawable.apple1
                            else
                                R.drawable.apple2
                            local.productName = R.string.product_simple_title.obtainString()
                            local.productType = R.string.product_simple_type.obtainString()
                            local.productPrice = "20499 - $childIndex"
                        }
                    )
                }
            }
        }
    }

}