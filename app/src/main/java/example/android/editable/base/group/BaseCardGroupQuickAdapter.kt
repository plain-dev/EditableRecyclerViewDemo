package example.android.editable.base.group

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.entity.SectionEntity
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import example.android.editable.R
import example.android.editable.ktx.obtainDimension
import example.android.editable.ktx.obtainDimensionPixelSize

const val CARD_TYPE_HEADER = 201
const val CARD_TYPE_BODY = 202
const val CARD_TYPE_FOOTER = 203
const val CARD_TYPE_HEADER_AND_FOOTER = 204

/**
 * 卡片分组列表适配器
 */
abstract class BaseCardGroupQuickAdapter<T : GroupEntity, VH : BaseViewHolder> @JvmOverloads constructor(
    @LayoutRes private val sectionHeadResId: Int,
    @LayoutRes private val layoutResId: Int,
    data: MutableList<T>? = null
) : BaseSectionQuickAdapter<T, VH>(sectionHeadResId, layoutResId, data) {

    override fun convertHeader(helper: VH, item: T) {
        handleGroup(helper)
    }

    override fun convert(helper: VH, item: T) {
        handleGroup(helper)
    }

    private fun handleGroup(holder: VH) {
        val cardView = getMaterialCardView(holder)
        val lineView = getLineView(holder)
        val adapterPosition = holder.adapterPosition
        val cardViewType = findCardViewTypeByPositionV2(data, adapterPosition)
        if (cardView != null) {
            adjustCardViewCornerRadiusWithType(
                cardView = cardView,
                cardViewType = cardViewType,
                cornerRadius = getCardViewCornerRadius()
                    ?: R.dimen.cardView_radius.obtainDimension()
            )
            adjustCardViewSectionTopMargin(
                cardView = cardView,
                margin = getCardViewLastBottomMargin()
                    ?: R.dimen.dp20.obtainDimensionPixelSize(),
                isHeader = data[adapterPosition].isHeader
            )
            adjustCardViewLastBottomMargin(
                cardView = cardView,
                margin = getCardViewLastBottomMargin()
                    ?: R.dimen.dp10.obtainDimensionPixelSize(),
                isLastPosition = adapterPosition == data.size - 1
            )
        }
        if (lineView != null) {
            adjustCardViewLine(
                lineView = lineView,
                cardViewType = cardViewType
            )
        }
    }

    private fun adjustCardViewSectionTopMargin(
        cardView: MaterialCardView,
        margin: Int,
        isHeader: Boolean
    ) {
        cardView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = if (isHeader) {
                margin
            } else {
                0
            }
        }
    }

    protected open fun getMaterialCardView(holder: VH): MaterialCardView? = null

    protected open fun getCardViewCornerRadius(): Float? = null

    protected open fun getCardViewLastBottomMargin(): Int? = null

    protected open fun getLineView(holder: VH): View? = null

    private fun adjustCardViewLine(
        lineView: View,
        cardViewType: Int
    ) {
        lineView.isVisible = cardViewType == CARD_TYPE_HEADER || cardViewType == CARD_TYPE_BODY
    }

    private fun adjustCardViewLastBottomMargin(
        cardView: MaterialCardView,
        margin: Int,
        isLastPosition: Boolean
    ) {
        cardView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = if (isLastPosition) {
                margin
            } else {
                0
            }
        }
    }

    private fun adjustCardViewCornerRadiusWithType(
        cardView: MaterialCardView,
        cardViewType: Int,
        cornerRadius: Float
    ) {
        when (cardViewType) {
            CARD_TYPE_HEADER -> adjustCardViewCornerRadius(
                cardView = cardView,
                topRight = cornerRadius,
                topLeft = cornerRadius,
                bottomRight = 0f,
                bottomLeft = 0f
            )
            CARD_TYPE_BODY -> adjustCardViewCornerRadius(
                cardView = cardView,
                topRight = 0f,
                topLeft = 0f,
                bottomRight = 0f,
                bottomLeft = 0f
            )
            CARD_TYPE_FOOTER -> adjustCardViewCornerRadius(
                cardView = cardView,
                topRight = 0f,
                topLeft = 0f,
                bottomRight = cornerRadius,
                bottomLeft = cornerRadius
            )
            CARD_TYPE_HEADER_AND_FOOTER -> adjustCardViewCornerRadius(
                cardView = cardView,
                topRight = cornerRadius,
                topLeft = cornerRadius,
                bottomRight = cornerRadius,
                bottomLeft = cornerRadius
            )
        }
    }

    private fun adjustCardViewCornerRadius(
        cardView: MaterialCardView,
        topRight: Float,
        topLeft: Float,
        bottomRight: Float,
        bottomLeft: Float
    ) {
        val builder = cardView.shapeAppearanceModel
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, topRight)
            .setTopLeftCorner(CornerFamily.ROUNDED, topLeft)
            .setBottomRightCorner(CornerFamily.ROUNDED, bottomRight)
            .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeft)
            .build()
        cardView.shapeAppearanceModel = builder
    }

    private fun findCardViewTypeByPosition(data: List<SectionEntity>, position: Int): Int {
        //val currentItem = data[position] // 当前
        val prePos = position - 1
        val nextPos = position + 1
        val indices = data.indices
        var preItem: SectionEntity? = null
        var nextItem: SectionEntity? = null
        if (indices.contains(prePos)) {
            preItem = data[prePos] // 上一个
        }
        if (indices.contains(nextPos)) {
            nextItem = data[nextPos] // 下一个
        }
        return if ((preItem != null && preItem.isHeader) && ((nextItem != null && nextItem.isHeader) || position == data.size - 1)) {
            CARD_TYPE_HEADER_AND_FOOTER
        } else if (preItem != null && preItem.isHeader) {
            CARD_TYPE_HEADER
        } else if ((nextItem != null && nextItem.isHeader) || position == data.size - 1) {
            CARD_TYPE_FOOTER
        } else {
            CARD_TYPE_BODY
        }
    }

    private fun findCardViewTypeByPositionV2(data: List<SectionEntity>, position: Int): Int {
        val currentItem = data[position] // 当前
        val prePos = position - 1
        val nextPos = position + 1
        val indices = data.indices
        var preItem: SectionEntity? = null
        var nextItem: SectionEntity? = null
        if (indices.contains(prePos)) {
            preItem = data[prePos] // 上一个
        }
        if (indices.contains(nextPos)) {
            nextItem = data[nextPos] // 下一个
        }
        return if (currentItem.isHeader) {
            CARD_TYPE_HEADER
        } else if ((preItem != null && preItem.isHeader) && ((nextItem != null && nextItem.isHeader) || position == data.size - 1)) {
            CARD_TYPE_HEADER_AND_FOOTER
        } else if ((nextItem != null && nextItem.isHeader) || position == data.size - 1) {
            CARD_TYPE_FOOTER
        } else {
            CARD_TYPE_BODY
        }
    }

    /*
    ------------------------------

                数据更改

    ------------------------------
    */

    fun removeGroupItem(position: Int) {
        val item = data[position]
        // 分组里只有一个元素
        if (data.isGroupSingleItem(item)) {
            // 把头部也删除
            val headerItem = data.findHeaderItem(item)
            if (headerItem != null) {
                super.remove(headerItem)
            }
        }
        super.remove(item)
    }

    override fun removeAt(position: Int) {
        if (position >= data.size) {
            return
        }
        this.data.removeAt(position)
        val internalPosition = position + headerLayoutCount
        notifyItemRemoved(internalPosition)
        compatibilityDataSizeChanged(0)
        var refreshPos = internalPosition - 1
        if (refreshPos < 0) refreshPos = 0
        notifyItemRangeChanged(refreshPos, this.data.size - internalPosition + refreshPos + 1)
    }

}