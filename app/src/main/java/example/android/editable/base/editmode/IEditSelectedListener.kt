package example.android.editable.base.editmode

/**
 * 编辑模式 选择监听器
 */
interface IEditSelectedListener<T> {

    /**
     * 回调当前选中的Item数量[count]
     */
    fun onSelectedItem(selectedList: List<T>, count: Int)

    /**
     * 回调长按进入编辑模式
     */
    fun onLongClickEnterEditMode()

    /**
     * 单选需要确认生效
     */
    fun onSingleSelectNeedConfirmation(
        address: T,
        isSelected: Boolean,
        confirmBlock: () -> Unit,
        failedBlock: () -> Unit
    ) {
        confirmBlock()
    }

}