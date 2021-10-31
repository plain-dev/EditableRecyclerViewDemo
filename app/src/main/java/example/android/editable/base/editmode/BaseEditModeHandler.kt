@file:Suppress("NAME_SHADOWING", "UNCHECKED_CAST", "NotifyDataSetChanged", "UNUSED")

package example.android.editable.base.editmode

import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import example.android.editable.base.SectionSelectEntity
import example.android.editable.base.group.findHeaderItem
import example.android.editable.ktx.findMethod
import example.android.editable.ktx.swap

/**
 * # 列表编辑模式处理器
 *
 * > 在 [BaseQuickAdapter] 及其子类中实例化此类，传入 [adapter] 对象
 *
 * ## 外部监听 & 联动
 *
 * - [editSelectedListener] 设置监听方法
 * - [bindExternalCheckBox] 联动外部全选按钮
 *
 * ## 切换模式 [changeMode]
 *
 * > 仅在 [onlyEditMode] 设为 `false` 时有效
 *
 * - [EDIT_MODE] 编辑模式
 * - [SHOW_MODE] 展示模式
 *
 * ## 获取视图
 *
 * - 获取复选按钮 [getCheckBox] 视图 (必须)
 * - 获取隐藏区域 [getHideView] 视图 (可选，仅在 [onlyEditMode] 设为 `false` 时有效)
 *
 * ## 获取选择模式 [getSelectMode]
 *
 * - [SELECT_MODE_PARENT] 点击「整个条目」视图触发选择
 * - [SELECT_MODE_CHILD] 点击「复选框」触发选择
 *
 * ## 获取选择类型 [getSelectType]
 *
 * - [SELECT_TYPE_MULTI] 多选
 * - [SELECT_TYPE_SINGLE] 单选
 *
 * ## 通用配置项
 *
 * - [onlyEditMode] 是否永远处于编辑模式 (default: false)
 * - [isRestoreUnselectedWhenChangeMode] 是否在 [changeMode] 时将所有「已选择项」置为「未选中」(default: true)
 *
 * ## 仅适用于单选配置项
 *
 * - [isSelectAlwaysTop] 选择项是否置顶 (default: true)
 * - [isSelectLeastOne] 是否最少选择一项 (单选必须选中一项，default: true)
 *
 * ## 更新和修改列表数据需主动调用以下方法
 *
 * - [setList] 适配器重写 `setList` 方法，并调用此方法
 * - [addData] 适配器重写 `addData` 方法，并调用此方法
 * - [remove] 适配器重写 `remove` 方法，并调用此方法
 * - [removeAt] 适配器重写 `removeAt` 方法，并调用此方法
 *
 */
abstract class BaseEditModeHandler<T : SectionSelectEntity, VH : BaseViewHolder>(private val adapter: BaseQuickAdapter<T, VH>) {

    companion object {

        private const val TAG = "EditModeHandler"

        const val SHOW_MODE = 0x101 // 展示模式
        const val EDIT_MODE = 0x202 // 编辑模式
        const val SELECT_MODE_PARENT = 0x201 // 选择模式 - 整个item
        const val SELECT_MODE_CHILD = 0x202 // 选择模式 - 仅CheckBox
        const val SELECT_TYPE_MULTI = 0x301 // 选择类型 - 多选
        const val SELECT_TYPE_SINGLE = 0x302 // 选择类型 - 单选

    }

    var currentMode = SHOW_MODE
        set(value) {
            if (field != value) field = value
        }

    private var oldItemClickListener: OnItemClickListener? = null

    var editSelectedListener: IEditSelectedListener<T>? = null

    private val selectedList = mutableMapOf<Int, SectionSelectEntity>()

    private var externalCheckBox: CheckBox? = null

    private val isSingleSelectType: Boolean
        get() = getSelectType() == SELECT_TYPE_SINGLE

    init {
        checkOnlyEditMode()
        initialSelectedList(adapter.data)
    }

    private fun checkOnlyEditMode() {
        if (onlyEditMode()) currentMode = EDIT_MODE
    }

    private fun initialSelectedList(data: Collection<T>?) {
        selectedList.clear()
        val data = data as Collection<SectionSelectEntity>
        val selected = data.filter { it.isSelected }
        if (isSingleSelectType) {
            val single = selected.getOrNull(0)
            if (single != null) {
                appendItemForSelectedList(single as T)
            }
        } else {
            selected.forEach { appendItemForSelectedList(it as T) }
        }
    }

    // ====================== Override local =========================//

    fun setList(list: Collection<T>?) {
        initialSelectedList(list)
        editSelectedListener?.onSelectedItem(getSelectedList(), getSelectedItemCount())
        externalCheckBox?.isChecked = false
    }

    fun addData(newData: Collection<T>) {
        externalCheckBox?.isChecked = false
    }

    fun removeAt(position: Int) {
        selectedList.remove(position)
    }

    fun remove(data: T) {
        selectedList.remove(adapter.data.indexOf(data))
    }

    fun convert(holder: VH, item: T) {
        // 处理编辑模式的核心方法
        editKernel(holder, item)
    }

    /**
     * 获取指定的复选框
     *
     * @param helper [BaseViewHolder]
     * @return [CheckBox]复选框
     */
    abstract fun getCheckBox(helper: VH): CheckBox?

    /**
     * 获取当切换显示模式 [SHOW_MODE]和 编辑模式 [EDIT_MODE] 时, 需要隐藏的View
     *
     * - 一般为复选框
     * - 在设置 [onlyEditMode] 为 true 时无效
     */
    open fun getHideView(helper: VH): View? {
        return null
    }

    /**
     * 获取选择模式
     *
     * 默认 [SELECT_MODE_PARENT]
     *
     */
    open fun getSelectMode(): Int {
        return SELECT_MODE_PARENT
    }

    /**
     * 获取选择类型
     *
     * 默认 [SELECT_TYPE_MULTI]
     */
    open fun getSelectType(): Int {
        return SELECT_TYPE_MULTI
    }

    /**
     * 永远处于编辑状态
     */
    open fun onlyEditMode(): Boolean = false

    /**
     * 切换模式后是否取消选中
     */
    open fun isRestoreUnselectedWhenChangeMode(): Boolean = true

    /**
     * 仅适用于单选类型 [SELECT_TYPE_SINGLE]
     *
     * - 默认最少选择一项
     */
    open fun isSelectLeastOne(): Boolean = true

    /**
     * 仅适用于单选类型 [SELECT_TYPE_SINGLE]
     *
     * - 选中项是否永远处于顶部
     */
    open fun isSelectAlwaysTop(): Boolean = true

    /**
     * 编辑模式核心
     */
    private fun editKernel(vh: VH, t: T) {
        val hideView = getHideView(vh)
        if (currentMode == EDIT_MODE) {
            hideView?.visibility = View.VISIBLE
            // 如果进入编辑模式，则进入编辑模式核心方法
            touchModeKernel(vh, t)
        } else {
            hideView?.visibility = View.GONE
            // 将切换到编辑模式前的 `ItemClickListener`，复原
            if (oldItemClickListener != null) {
                vh.itemView.setOnClickListener {
                    var position: Int = vh.adapterPosition
                    if (position == RecyclerView.NO_POSITION) {
                        return@setOnClickListener
                    }
                    position -= adapter.headerLayoutCount
                    adapter.setOnItemClickListener(oldItemClickListener)
                    adapter.findMethod(
                        "setOnItemClick",
                        View::class.java,
                        Int::class.java
                    ).invoke(
                        it,
                        position
                    )
                }
            }
            // 长按item进入编辑模式，进入的操作由外部实现
            vh.itemView.setOnLongClickListener {
                // 长按item判断下当前所处模式，如果是编辑模式就不响应
                if (!checkEditMode() && null != editSelectedListener) {
                    editSelectedListener?.onLongClickEnterEditMode()
                    appendItemForSelectedList(t)
                    callBackSelectedCount()
                    return@setOnLongClickListener true
                }
                false
            }
        }
    }

    /**
     * 根据当前[getSelectMode]选择模式，调用不用的方案
     */
    private fun touchModeKernel(vh: VH, t: T) {
        when {
            getSelectMode() == SELECT_MODE_PARENT -> {
                selectModeParentKernel(vh, t)
            }
            getSelectMode() == SELECT_MODE_CHILD -> {
                selectModeChildKernel(vh, t)
            }
            else -> {
                throw IllegalArgumentException(
                    "未指定触摸模式，重写 `getTouchMode()` 进行指定，" +
                            "可选 `SELECT_MODE_PARENT` 和 `SELECT_MODE_CHILD` !"
                )
            }
        }
    }

    /**
     * 选择模式[SELECT_MODE_PARENT]核心
     */
    private fun selectModeParentKernel(vh: VH, t: T) {
        val itemView: View = vh.itemView
        val checkBox = getCheckBox(vh)
        if (checkBox != null) {
            checkBox.isClickable = false
            checkBox.isChecked = t.isSelected
            // 缓存在 `SHOW_MODE` 时设置的 `OnItemClickListener`, 切换回去后进行还原
            oldItemClickListener = adapter.getOnItemClickListener()
            itemView.setOnClickListener {
                val selected = !t.isSelected
                if (isSingleSelectType && selected) return@setOnClickListener
                checkBox.isChecked = selected
                processSelected(t, selected)
                callBackSelectedCount()
            }
        }
    }

    /**
     * 选择模式[SELECT_MODE_CHILD]核心
     */
    private fun selectModeChildKernel(vh: VH, t: T) {
        val checkBox = getCheckBox(vh) ?: throw IllegalArgumentException("未指定CheckBox!!!")
        checkBox.isClickable = true
        checkBox.isChecked = t.isSelected
        if (currentMode == EDIT_MODE) {
            checkBox.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener //过滤非人为点击

                val index = adapter.data.indexOf(t)

                if (isSingleSelectType // 单选模式时
                    && !isChecked // 取消选中当前项
                    && isSelectLeastOne() // 如果指定最低选择一项
                    && selectedList.size == 1 // 已选择列表中仅一项
                    && selectedList[index] == t // 且为当前选择项
                ) {
                    buttonView.isChecked = true // 则不允许取消选中
                    return@setOnCheckedChangeListener
                }
                if (isSingleSelectType) {
                    if (isChecked) { // true
                        handleSingleSelected(buttonView, t, index)
                    } else { // false
                        handleSingleUnselected(buttonView, t, index)
                    }
                } else {
                    processSelected(t, isChecked)
                    callBackSelectedCount()
                }
            }
        }
    }

    /**
     * 处理单选选中
     */
    private fun handleSingleSelected(
        buttonView: CompoundButton,
        item: T,
        position: Int
    ) {
        // 选择操作
        val selectedBlock = {
            processSelected(item, true)
            val notifyPos = mutableListOf(position)
            val waitRemoveKeys = mutableListOf<Int>()
            selectedList.forEach {
                if (it.value != item) {
                    notifyPos.add(it.key)
                    waitRemoveKeys.add(it.key)
                    it.value.isSelected = false
                }
            }
            waitRemoveKeys.forEach { key ->
                selectedList.remove(key)
            }
            notifyPos.forEach {
                adapter.notifyItemChanged(it)
            }
        }

        // 选择项永远处于顶部
        val configBlock = {
            if (isSelectAlwaysTop() && position != 0) {
                adapter.data.swap(0, position)
                if (selectedList.containsKey(position)) {
                    selectedList.remove(position)
                    selectedList[0] = item
                }
                adapter.notifyItemChanged(0)
                adapter.notifyItemChanged(position)
            }
        }

        // 成功操作
        val finalBlock = {
            selectedBlock()
            configBlock()
        }

        // 失败操作
        val failedBlock = {
            buttonView.isChecked = false
        }

        editSelectedListener?.also {
            it.onSingleSelectNeedConfirmation(
                address = item,
                isSelected = true,
                confirmBlock = finalBlock,
                failedBlock = failedBlock
            )
        } ?: also {
            finalBlock()
        }
    }

    /**
     * 处理单选未选中
     */
    private fun handleSingleUnselected(
        buttonView: CompoundButton,
        item: T,
        position: Int
    ) {
        val unSelectedBlock = {
            if (selectedList.containsKey(position)) {
                selectedList.remove(position)
            }
            item.isSelected = false
            adapter.notifyItemChanged(position)
        }

        // 失败操作
        val failedBlock = {
            buttonView.isChecked = true
        }

        editSelectedListener?.also {
            it.onSingleSelectNeedConfirmation(
                address = item,
                isSelected = false,
                confirmBlock = unSelectedBlock,
                failedBlock = failedBlock
            )
        } ?: also {
            unSelectedBlock()
        }
    }

    /**
     * 选择状态设置
     */
    private fun processSelected(t: T, isSelected: Boolean) {
        if (isSelected) {
            // 联动分组条目选中
            linkageGroupItemSelected(t)
            // 选择本条目
            appendItemForSelectedList(t)
        } else {
            // 联动分组条目未选中
            linkageGroupItemUnselected(t)
            // 取消选中本条目
            removeItemForSelectedList(t)
        }
    }

    /**
     * 联动分组条目选中
     */
    private fun linkageGroupItemSelected(t: T) {
        // 如果点击的是父项
        if (t.isHeader) {
            val notifyPos = mutableListOf<Int>()
            val unselectedChildList = adapter.data.filter {
                it != t && it.groupId == t.groupId && !it.isSelected
            }
            // 将未选中子项加入到选中列表
            unselectedChildList.forEach { t ->
                notifyPos.add(adapter.data.indexOf(t))
                appendItemForSelectedList(t)
            }
            // 刷新UI
            notifyPos.forEach { adapter.notifyItemChanged(it) }
        } else {
            // 寻找不是自己
            // 不是头部
            // 并且未选中的项
            // 如果找不到，说明本组都选中了
            val isGroupExistUnselected = adapter.data.any {
                it != t && !it.isHeader && it.groupId == t.groupId && !it.isSelected
            }
            if (!isGroupExistUnselected) {
                // 找到本组的头部
                val header = adapter.data.findHeaderItem(t)
                if (header != null) {
                    appendItemForSelectedList(header)
                    adapter.notifyItemChanged(adapter.data.indexOf(header))
                }
            }
        }
    }

    /**
     * 联动分组条目未选中
     */
    private fun linkageGroupItemUnselected(t: T) {
        if (t.isHeader) {
            val notifyPos = mutableListOf<Int>()
            val selectedChildList = adapter.data.filter {
                it != t && it.groupId == t.groupId && it.isSelected
            }
            // 将选中子项加入到未选中列表
            selectedChildList.forEach { t ->
                notifyPos.add(adapter.data.indexOf(t))
                removeItemForSelectedList(t)
            }
            // 刷新UI
            notifyPos.forEach { adapter.notifyItemChanged(it) }
        } else {
            // 找到本组的头部，如果是选中，就置为未选中
            val header = adapter.data.findHeaderItem(t)
            if (header != null && header.isSelected) {
                removeItemForSelectedList(header)
                adapter.notifyItemChanged(adapter.data.indexOf(header))
            }
        }
    }

    /**
     * 添加Item到已选择列表[selectedList]
     */
    private fun appendItemForSelectedList(t: T) {
        t.isSelected = true
        // 元素位置为 key
        if (!selectedList.containsValue(t)) {
            selectedList[adapter.data.indexOf(t)] = t
        }
    }

    /**
     * 删除Item从已选择列表[selectedList]
     *
     * true: [selectedList] 包含此元素 false: [selectedList] 不包含此元素
     */
    private fun removeItemForSelectedList(t: T): Boolean {
        t.isSelected = false
        if (selectedList.containsValue(t)) {
            selectedList.remove(adapter.data.indexOf(t))
            return true
        }
        return false
    }

    /**
     * 变更显示模式
     *
     * 显示模式: [SHOW_MODE]
     * 编辑模式: [EDIT_MODE]
     */
    open fun changeMode(mode: Int) {
        if (onlyEditMode()) {
            Log.d(TAG, "[changeMode] 指令无效，该适配器永远处于编辑状态")
            return
        }
        // 切换到显示模式，外部 `CheckBox` 复原到未选择状态
        externalCheckBox?.apply {
            if (mode == SHOW_MODE && isChecked) {
                isChecked = false
            }
        }
        currentMode = mode
        if (isRestoreUnselectedWhenChangeMode()) {
            restoreUnselected()
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * 恢复所有已选择Item[selectedList]为未选择状态
     */
    private fun restoreUnselected() {
        if (selectedList.isNotEmpty()) {
            for (selected in selectedList) {
                if (selected.value.isSelected) {
                    selected.value.isSelected = false
                }
            }
            selectedList.clear()
        }
    }

    /**
     * 选择全部Item
     */
    open fun selectedAllItem() {
        if (isSingleSelectType) return
        if (adapter.data.isNotEmpty() && checkEditMode()) {
            for (i in adapter.data.indices) {
                val t = adapter.data[i]
                appendItemForSelectedList(t)
                adapter.notifyItemChanged(i)
            }
            callBackSelectedCount()
        }
    }

    /**
     * 取消选择全部Item
     */
    open fun unSelectedAllItem() {
        if (isSingleSelectType) return
        if (adapter.data.isNotEmpty() && checkEditMode()) {
            for (i in adapter.data.indices) {
                val t = adapter.data[i]
                removeItemForSelectedList(t)
                adapter.notifyItemChanged(i)
            }
            callBackSelectedCount()
        }
    }

    /**
     * 删除选中Item[selectedList]
     */
    open fun removeSelectedItem() {
        if (adapter.data.isNotEmpty() && checkEditMode()) {
            // 循环内删除元素需要倒序删除
            for (i in adapter.data.indices.reversed()) {
                val t = adapter.data[i]
                if (removeItemForSelectedList(t)) {
                    adapter.data.removeAt(i)
                    removeItem(i)
                }
            }
            callBackSelectedCount()
        }
    }

    /**
     * 获取已选择Item[selectedList]的数量
     */
    open fun getSelectedItemCount(): Int {
        return if (selectedList.isNotEmpty() && checkEditMode()) selectedList.size else 0
    }

    /**
     * 删除Item[BaseQuickAdapter.data]
     */
    private fun removeItem(pos: Int) {
        adapter.notifyItemRemoved(pos)
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    /**
     * 获取是否选择全部数据
     *
     * true: 选择全部
     * false: 未选中全部
     */
    open fun isSelectedAllItem(): Boolean {
        return getSelectedItemCount() == adapter.data.size
    }

    /**
     * 回调当前选择数量
     */
    private fun callBackSelectedCount() {
        if (null != editSelectedListener) {
            val count = getSelectedItemCount()
            // 检查是否选中全部，并联动外部 `CheckBox` 状态
            externalCheckBox?.isChecked = count > 0 && count == adapter.data.size
            // 排除头部
            val excludeHeader = getSelectedList().filter { !it.isHeader }
            editSelectedListener?.onSelectedItem(excludeHeader, excludeHeader.size)
        }
    }

    /**
     * 检查当前是否处于[EDIT_MODE]编辑模式
     *
     * true: 编辑模式[EDIT_MODE]
     * false: 显示模式[SHOW_MODE]
     */
    private fun checkEditMode(): Boolean {
        return currentMode == EDIT_MODE
    }

    /**
     * 获取已选择的Item[selectedList]
     */
    open fun getSelectedList(): List<T> {
        return selectedList.values.toList() as List<T>
    }

    /**
     * 获取要删除的Item (外部实现)
     *
     * 一般删除item都是需要传给接口id的，可以在适配器中重写此方法，来实现自己的逻辑
     * 获取以选择的item集合可通过[getSelectedList]获得
     *
     */
    open fun getDeleteParams(): String? {
        return null
    }

    /**
     * 绑定外部CheckBox[externalCheckBox]，使其跟随列表联动
     */
    @JvmOverloads
    open fun bindExternalCheckBox(
        externalCheckBox: CheckBox?,
        checkedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    ) {
        if (null != externalCheckBox) {
            this.externalCheckBox = externalCheckBox
            externalCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                checkedChangeListener?.onCheckedChanged(buttonView, isChecked)
                if (!buttonView.isPressed) return@OnCheckedChangeListener
                if (getSelectType() != SELECT_TYPE_SINGLE && adapter.data.isNotEmpty()) {
                    if (!isChecked || isSelectedAllItem()) {
                        unSelectedAllItem() // 取消全选
                    } else {
                        selectedAllItem() // 全选
                    }
                } else {
                    buttonView.isChecked = false
                }
            })
        }
    }

}