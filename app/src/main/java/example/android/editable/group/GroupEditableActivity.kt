@file:Suppress("SetTextI18n")

package example.android.editable.group

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import example.android.editable.R
import example.android.editable.base.editmode.IEditSelectedListener
import example.android.editable.debug.SimpleData
import example.android.editable.group.adapter.GroupSectionEditableAdapter
import example.android.editable.group.data.GroupData
import example.android.editable.ktx.obtainString
import kotlinx.android.synthetic.main.activity_group_editable.*

class GroupEditableActivity : AppCompatActivity() {

    private val groupSectionEditableAdapter by lazy {
        GroupSectionEditableAdapter().apply {
            editModeHandle.editSelectedListener = object : IEditSelectedListener<GroupData> {
                override fun onSelectedItem(selectedList: List<GroupData>, count: Int) {
                    Log.e("Editable", "[onSelectedItem] -> ${selectedList.size}")
                    tvTotalCount?.text = R.string.items_selected.obtainString(count)
                }

                override fun onLongClickEnterEditMode() {

                }
            }
            editModeHandle.bindExternalCompoundButton(checkAll)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_editable)
        title = R.string.cart.obtainString()
        initList()
        loadData()
    }

    private fun initList() {
        rvList?.apply {
            // 踩坑：在某些情况下调用 `notifyItemChanged` 通知刷新两个地方的条目
            // 出现另外一个条目视图没有变化的问题 (数据状态已经更改了)
            //    - 如果是多类型条目，不同组之间交换不会出现问题
            //    - 同组之间则问题出现
            // 此时调用 `notifyDataSetChanged` 则正常
            // 去掉这里的关闭刷新动画后，问题消失
            //(itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            layoutManager = LinearLayoutManager(context)
            adapter = groupSectionEditableAdapter
        }
    }

    private fun loadData() {
        groupSectionEditableAdapter.setList(SimpleData.getSimpleGroupData())
    }

}