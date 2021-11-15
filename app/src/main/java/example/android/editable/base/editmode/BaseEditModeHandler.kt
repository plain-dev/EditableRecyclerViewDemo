@file:Suppress("NAME_SHADOWING", "UNCHECKED_CAST", "NotifyDataSetChanged", "UNUSED")

package example.android.editable.base.editmode

import android.util.Log
import android.view.View
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
 * > 在 [BaseQuickAdapter] 及其子类中实例化此类，传入 [adapter] 对象，在适配器 `convert` 中调用 [convert] 方法
 *
 * ## 外部监听 & 联动
 *
 * - [editSelectedListener] 设置监听方法
 * - [bindExternalCompoundButton] 联动外部全选按钮
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
 * - 获取复选按钮 [getCompoundButton] 视图 (必须)
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
        const val SELECT_MODE_PARENT = 0x201 // 选择模式 - 整个 item
        const val SELECT_MODE_CHILD = 0x202 // 选择模式 - 仅 CompoundButton
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

    private var externalCompoundButton: CompoundButton? = null

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

    //<editor-fold name="Override local">

    fun setList(list: Collection<T>?) {
        initialSelectedList(list)
        editSelectedListener?.onSelectedItem(getSelectedList(), getSelectedItemCount())
        externalCompoundButton?.isChecked = false
    }

    fun addData(newData: Collection<T>) {
        externalCompoundButton?.isChecked = false
    }

    fun removeAt(position: Int) {
        selectedList.remove(position)
    }

    fun remove(data: T) {
        selectedList.remove(adapter.data.indexOf(data))
    }

    //</editor-fold>

    fun convert(holder: VH, item: T) {
        // 处理编辑模式的核心方法
        editKernel(holder, item)
    }

    /**
     * 获取指定的复合按钮
     *
     * @param helper [BaseViewHolder]
     * @return [CompoundButton] 复合按钮
     */
    abstract fun getCompoundButton(helper: VH): CompoundButton?

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
     * 是否长按选中
     *
     * - 当处于 [SHOW_MODE] 时有效
     *
     */
    open fun isLongClickSelected(): Boolean = false

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
            if (isLongClickSelected()) { // 长按 item 进入编辑模式
                vh.itemView.setOnLongClickListener {
                    if (!checkEditMode() && !onlyEditMode()) {
                        editSelectedListener?.onLongClickEnterEditMode()
                        changeMode(EDIT_MODE)
                        appendItemForSelectedList(t)
                        callBackSelectedCount()
                        return@setOnLongClickListener true
                    }
                    false
                }
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
        val buttonView =
            getCompoundButton(vh) ?: throw IllegalArgumentException("未指定 CompoundButton !!!")
        buttonView.isClickable = false
        buttonView.isChecked = t.isSelected
        if (currentMode == EDIT_MODE) {
            // 缓存在 `SHOW_MODE` 时设置的 `OnItemClickListener`, 切换回去后进行还原
            oldItemClickListener = adapter.getOnItemClickListener()
            itemView.setOnClickListener {
                val isSelected = !t.isSelected
                buttonView.isChecked = isSelected
                // 开始处理选择逻辑
                handleSingleOrMultiSelect(t, isSelected, buttonView)
            }
        }
    }

    /**
     * 选择模式[SELECT_MODE_CHILD]核心
     */
    private fun selectModeChildKernel(vh: VH, t: T) {
        val buttonView =
            getCompoundButton(vh) ?: throw IllegalArgumentException("未指定 CompoundButton !!!")
        buttonView.isClickable = true
        buttonView.isChecked = t.isSelected
        if (currentMode == EDIT_MODE) {
            buttonView.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener //过滤非人为点击
                // 开始处理选择逻辑
                handleSingleOrMultiSelect(t, isChecked, buttonView)
            }
        }
    }

    /**
     * 处理单选或多选逻辑 (开始)
     */
    private fun handleSingleOrMultiSelect(t: T, isSelected: Boolean, buttonView: CompoundButton) {
        val index = adapter.data.indexOf(t)

        if (checkSingleSelectedOnlyOne(t, isSelected, index)) {
            buttonView.isChecked = true // 不允许取消选中
            return
        }

        if (isSingleSelectType) {
            handleSingleSelect(buttonView, t, index, isSelected)
        } else {
            processSelected(t, isSelected)
            callBackSelectedCount()
        }
    }

    //<editor-fold name="单选逻辑">

    /**
     * 单选模式：检查是否仅选择一项
     */
    private fun checkSingleSelectedOnlyOne(t: T, isSelected: Boolean, index: Int): Boolean {
        if (isSingleSelectType // 单选模式时
            && !isSelected // 取消选中当前项
            && isSelectLeastOne() // 如果指定最低选择一项
            && selectedList.size == 1 // 已选择列表中仅一项
            && selectedList[index] == t // 且为当前选择项
        ) {
            return true
        }
        return false
    }

    /**
     * 处理单选 [SELECT_TYPE_SINGLE] 逻辑
     */
    private fun handleSingleSelect(
        buttonView: CompoundButton,
        t: T,
        index: Int,
        isSelected: Boolean
    ) {
        if (isSelected) { // 选中
            handleSingleSelected(buttonView, t, index)
        } else { // 未选中
            handleSingleUnselected(buttonView, t, index)
        }
    }

    /**
     * 处理单选 [SELECT_TYPE_SINGLE] 选中逻辑
     */
    private fun handleSingleSelected(
        buttonView: CompoundButton,
        item: T,
        index: Int
    ) {
        // 选择操作
        val selectedBlock = {
            processSelected(item, true)
            val notifyPos = mutableListOf(index)
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
            if (isSelectAlwaysTop() && index != 0) {
                adapter.data.swap(0, index)
                if (selectedList.containsKey(index)) {
                    selectedList.remove(index)
                    selectedList[0] = item
                }
                adapter.notifyItemChanged(index)
                adapter.notifyItemChanged(0)
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
     * 处理单选 [SELECT_TYPE_SINGLE] 未选中逻辑
     */
    private fun handleSingleUnselected(
        buttonView: CompoundButton,
        item: T,
        index: Int
    ) {
        val unSelectedBlock = {
            if (selectedList.containsKey(index)) {
                selectedList.remove(index)
            }
            item.isSelected = false
            adapter.notifyItemChanged(index)
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

    //</editor-fold>

    //<editor-fold name="通用选择逻辑">

    /**
     * 处理通用选择逻辑
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
     * 联动分组条目选中 (仅多选)
     */
    private fun linkageGroupItemSelected(t: T) {
        // 不适用于单选模式
        if (getSelectType() == SELECT_TYPE_SINGLE) return
        // 如果点击的是父项
        if (t.isHeader) {
            val notifyPos = mutableListOf<Int>()
            val unselectedChildList = adapter.data.filter {
                it != t && it.groupId == t.groupId && !it.isSelected
            }
            // 将未选中子项加入到选中列表
            unselectedChildList.forEach { item ->
                notifyPos.add(adapter.data.indexOf(item))
                appendItemForSelectedList(item)
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
     * 联动分组条目未选中 (仅多选)
     */
    private fun linkageGroupItemUnselected(t: T) {
        // 不适用于单选模式
        if (getSelectType() == SELECT_TYPE_SINGLE) return
        if (t.isHeader) {
            val notifyPos = mutableListOf<Int>()
            val selectedChildList = adapter.data.filter {
                it != t && it.groupId == t.groupId && it.isSelected
            }
            // 将选中子项加入到未选中列表
            selectedChildList.forEach { item ->
                notifyPos.add(adapter.data.indexOf(item))
                removeItemForSelectedList(item)
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

    //</editor-fold>

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
        // 切换到显示模式，外部 `CompoundButton` 复原到未选择状态
        externalCompoundButton?.apply {
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
     * 检查当前是否处于[EDIT_MODE]编辑模式
     *
     * true: 编辑模式[EDIT_MODE]
     * false: 显示模式[SHOW_MODE]
     */
    private fun checkEditMode(): Boolean {
        return currentMode == EDIT_MODE
    }

    /**
     * 回调当前选择数量
     */
    private fun callBackSelectedCount() {
        if (null != editSelectedListener) {
            val count = getSelectedItemCount()
            // 检查是否选中全部，并联动外部 `CompoundButton` 状态
            externalCompoundButton?.isChecked = count > 0 && count == adapter.data.size
            // 排除头部
            val excludeHeader = getSelectedList().filter { !it.isHeader }
            editSelectedListener?.onSelectedItem(excludeHeader, excludeHeader.size)
        }
    }

    //<editor-fold name="最终处理选择指令的方法">

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
    private fun selectedAllItem() {
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
    private fun unSelectedAllItem() {
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
     * 删除Item[BaseQuickAdapter.data]
     */
    private fun removeItem(pos: Int) {
        adapter.notifyItemRemoved(pos)
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    //</editor-fold>

    //<editor-fold name="获取已选择列表相关方法">

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
     * 获取已选择的Item[selectedList]
     */
    open fun getSelectedList(): List<T> {
        return selectedList.values.toList() as List<T>
    }

    /**
     * 获取已选择Item[selectedList]的数量
     */
    open fun getSelectedItemCount(): Int {
        return if (selectedList.isNotEmpty() && checkEditMode()) selectedList.size else 0
    }

    //</editor-fold>

    /**
     * 绑定外部复合按钮 [externalCompoundButton]，使其跟随列表联动
     *
     * - 全选/取消全选
     * - 仅限多选模式 [SELECT_TYPE_MULTI] 使用
     */
    @JvmOverloads
    open fun bindExternalCompoundButton(
        externalCompoundButton: CompoundButton?,
        checkedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    ) {
        if (null != externalCompoundButton) {
            this.externalCompoundButton = externalCompoundButton
            externalCompoundButton.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                checkedChangeListener?.onCheckedChanged(buttonView, isChecked)
                if (!buttonView.isPressed) return@OnCheckedChangeListener // Filter non-human clicks
                if (getSelectType() != SELECT_TYPE_SINGLE && adapter.data.isNotEmpty()) { // Only support multi-select
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