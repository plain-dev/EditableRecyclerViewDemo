package example.android.editable.debug

import example.android.editable.group.data.GroupData

object SimpleData {

    fun getSimpleGroupData(): MutableList<GroupData> {

        return mutableListOf<GroupData>().apply {
            (1..6).forEach { sectionIndex->
                val groupId = "100${sectionIndex}"
                add(
                    GroupData().also { local ->
                        local.groupId = groupId
                        local.parentTitle = "Here is the group title - $sectionIndex"
                    }
                )
                (1..(1..6).random()).forEach {childIndex->
                    add(
                        GroupData().also { local ->
                            local.groupId = groupId
                            local.name = "Here is the sub-item title - $sectionIndex-$childIndex"
                        }
                    )
                }
            }
        }
    }

}