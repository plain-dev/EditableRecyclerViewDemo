package example.android.editable.base

import example.android.editable.base.group.GroupEntity

abstract class SectionSelectEntity : GroupEntity() {

    open var isSelected: Boolean = false

}