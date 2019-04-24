package com.xjy.table

import android.widget.TextView

interface SXTableDataSource {
    fun numOfColumn(): Int
    fun numOfRow(): Int
    fun configCell(textView: TextView, rowIndex: Int, columnIndex: Int)
    fun configColumnHeader(textView: TextView, columnIndex: Int){

    }
    fun configTitle(textView: TextView)
    fun titleOfColumn(columnIndex: Int):String

    //返回单元格的宽度，-1则自适应撑开
    fun widthOfCell(columnIndex: Int):Int

    fun title():String

    //id相同则合并单元格，id随自己定
    fun idOfCell(rowIndex: Int, columnIndex: Int): String {
        return "$rowIndex$columnIndex"
    }

}