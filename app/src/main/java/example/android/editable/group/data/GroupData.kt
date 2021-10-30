package example.android.editable.group.data

import example.android.editable.base.SectionSelectEntity

class GroupData : SectionSelectEntity() {

    var parentTitle: String = ""

    var name: String = ""

    override val isHeader: Boolean
        get() = parentTitle.isNotEmpty()

}