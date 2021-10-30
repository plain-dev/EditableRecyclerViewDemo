package example.android.editable.group.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import example.android.editable.R
import example.android.editable.base.group.BaseCardGroupQuickAdapter
import example.android.editable.base.editmode.BaseEditModeHandler
import example.android.editable.group.data.GroupData
import example.android.editable.ktx.obtainDimension
import example.android.editable.ktx.obtainDimensionPixelOffset

class GroupSectionEditableAdapter(
    // ignore
) : BaseCardGroupQuickAdapter<GroupData, GroupSectionEditableViewHolder>(
    R.layout.item_section, R.layout.item_child
) {

    val editModeHandle = object :BaseEditModeHandler<GroupData,GroupSectionEditableViewHolder>(this@GroupSectionEditableAdapter){

        override fun getCheckBox(helper: GroupSectionEditableViewHolder): CheckBox? {
            return helper.getViewOrNull(R.id.check)
        }

        override fun getSelectMode() = SELECT_MODE_CHILD

        override fun getSelectType() = SELECT_TYPE_MULTI

        override fun onlyEditMode() = true

    }

    override fun convert(helper: GroupSectionEditableViewHolder, item: GroupData) {
        super.convert(helper, item)
        editModeHandle.convert(helper, item)
        helper.getViewOrNull<TextView>(R.id.tvChildTitle)?.text = item.name
    }

    override fun convertHeader(helper: GroupSectionEditableViewHolder, item: GroupData) {
        super.convertHeader(helper, item)
        editModeHandle.convert(helper, item)
        helper.getViewOrNull<TextView>(R.id.tvSectionTitle)?.text = item.parentTitle
    }

    override fun getMaterialCardView(holder: GroupSectionEditableViewHolder): MaterialCardView? {
        return holder.getViewOrNull(R.id.cardView)
    }

    override fun getCardViewCornerRadius() = R.dimen.dp10.obtainDimension()

    override fun getCardViewLastBottomMargin() = R.dimen.dp20.obtainDimensionPixelOffset()

    override fun getLineView(holder: GroupSectionEditableViewHolder): View? {
        return holder.getViewOrNull(R.id.line)
    }

}