package com.xjy.table

import android.widget.TextView

class SimpleTableDataSource : SXTableDataSource {
    override fun configTitle(textView: TextView) {

    }

    override fun title(): String {
        return "表格"
    }

    override fun numOfColumn(): Int {
        return 5
    }

    override fun numOfRow(): Int {
        return 6
    }

    override fun configCell(textView: TextView, rowIndex: Int, columnIndex: Int) {
        textView.text = "$rowIndex-$columnIndex"
        if (rowIndex == 3 && columnIndex == 1){
            textView.text = "很像很奥古斯丁噶岁的嘎斯U盾干哈岁的哈斯"
        }

    }

    override fun titleOfColumn(columnIndex: Int): String {
        return "标题$columnIndex"
    }

    override fun widthOfCell(columnIndex: Int): Int {
        return if (columnIndex == 2) -1 else 160
//        return true
    }

    override fun idOfCell(rowIndex: Int, columnIndex: Int): String {
        if (columnIndex == 0) {
            if (rowIndex < 3) {
                return "aaaa"
            }
        }
        if (columnIndex == 4){
            if (rowIndex < 5){
                return "bbbb"
            }
        }
        if (rowIndex == 5){
            if (columnIndex < 2){
                return "cccc"
            }
        }
        return super.idOfCell(rowIndex, columnIndex)
    }

}

