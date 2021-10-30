package example.android.editable.base.group

/*
------------------------------

        卡片分组帮助类

------------------------------
 */

/**
 * 此 [item] 是否为分组里唯一一个
 */
fun <T : GroupEntity> List<T>?.isGroupSingleItem(item: T): Boolean {
    val data = this
    if (data.isNullOrEmpty()) return false
    return data.filter { !it.isHeader && it.groupId == item.groupId }.count() == 1
}

/**
 * 查找此 [item] 分组头部
 */
fun <T : GroupEntity> List<T>?.findHeaderItem(item: T): T? {
    val data = this
    if (data.isNullOrEmpty()) return null
    return data.find { it.isHeader && it.groupId == item.groupId }
}

/**
 * 获取 Child 大小
 */
fun <T : GroupEntity> List<T>?.getChildSize(): Int {
    return this?.filter { !it.isHeader }?.count() ?: 0
}