package com.xjy.table

import android.view.View

class SXTableColumnClick(val cloumn:Int, val listener: SXOnTableColumnClickListener): View.OnClickListener {

    override fun onClick(v: View?) {
        v?.let {
            listener.onColumnClick(v.tag as Int)
        }
    }
}