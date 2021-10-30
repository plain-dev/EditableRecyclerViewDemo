package example.android.editable.group

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import example.android.editable.R
import example.android.editable.base.editmode.IEditSelectedListener
import example.android.editable.debug.SimpleData
import example.android.editable.group.adapter.GroupSectionEditableAdapter
import example.android.editable.group.data.GroupData
import kotlinx.android.synthetic.main.activity_group_editable.*

class GroupEditableActivity : AppCompatActivity() {

    private val groupSectionEditableAdapter by lazy {
        GroupSectionEditableAdapter().apply {
            editModeHandle.editSelectedListener = object : IEditSelectedListener<GroupData> {
                @SuppressLint("SetTextI18n")
                override fun onSelectedItem(selectedList: List<GroupData>, count: Int) {
                    Log.e("LocalTest", "[onSelectedItem] ==>> ${selectedList.size}")
                    tvTotalCount?.text = "$count items selected"
                }

                override fun onLongClickEnterEditMode() {

                }
            }
            editModeHandle.bindExternalCheckBox(checkAll)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_editable)
        initList()
        loadData()
    }

    private fun initList() {
        rvList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupSectionEditableAdapter
        }
    }

    private fun loadData() {
        groupSectionEditableAdapter.setList(SimpleData.getSimpleGroupData())
    }

}