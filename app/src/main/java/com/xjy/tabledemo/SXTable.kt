package com.xjy.tabledemo

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.widget.TextView
import com.xjy.table.SXTableDataSource
import com.xjy.table.SXTableView
import com.xjy.table.Util

class SXTable: SXTableView, SXTableDataSource {

    private val data = mutableListOf<TableItemModel>()

    fun add(item:TableItemModel){
        data.add(item)
    }

    fun add(items:List<TableItemModel>){
        data.addAll(items)
    }

    fun addAll(items:List<TableItemModel>){
        data.clear()
        add(items)
    }

    override fun numOfColumn(): Int {
        return titles.count()
    }

    override fun numOfRow(): Int {
        return data.count()
    }

    override fun configCell(textView: TextView, rowIndex: Int, columnIndex: Int) {
        textView.text = "$rowIndex - $columnIndex"
        val item = data[rowIndex]
        when(columnIndex){
            //第一行显示序号
            0-> textView.text = item.ordernumber
            2-> textView.text = "Hello WOlrdashuidhas"
        }

    }

    override fun configTitle(textView: TextView) {
        textView.setBackgroundColor(Color.WHITE)
    }

    override fun titleOfColumn(columnIndex: Int): String {
        return titles[columnIndex]
    }

    override fun widthOfCell(columnIndex: Int): Int {
        return when(columnIndex){
            //这里可以修改固定几列的宽度
            0,3,4 -> Util.dip2px(context, 50)
            else -> -1
        }
    }

    override fun title(): String {
        return "传阅进度"
    }

    override fun idOfCell(rowIndex: Int, columnIndex: Int): String {
        if (columnIndex == 2 && rowIndex > 4 && rowIndex < 8){
            return "PY"
        }
        if (rowIndex == 2 && columnIndex < 2){
            return "Hello"
        }
        if (rowIndex == 2 && columnIndex > 2){
            return "Hello2"
        }

        if (rowIndex == 4 && columnIndex > 1 && columnIndex < 4){
            return "Hello4"
        }

        if (columnIndex == 0){
//            return data[rowIndex].ordernumber
        }



        return super.idOfCell(rowIndex, columnIndex)
    }

    fun getRowData(row:Int):TableItemModel{
        return data[row]
    }

    private val titles = mutableListOf("序号", "主办人", "抄送人", "确", "阅")

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        dataSource = this
    }

    fun setModel(model:NorHandlingDetail){
        data.clear()
        model.list.forEach {task->
            task.sendList.forEach {
                val item = TableItemModel(
                    ordernumber = task.ordernumber,
                    copy = task.copy,
                    status = it.status,
                    isRead = it.isRead,
                    copyList = task.copyList
                    )
                data.add(item)
            }
        }
        reload()
    }
}