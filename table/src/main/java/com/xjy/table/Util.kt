package com.xjy.table

import android.content.Context

object Util {


    public fun dip2px(context: Context, dpValue:Int):Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    public fun px2dip(context: Context , pxValue:Int):Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }
}