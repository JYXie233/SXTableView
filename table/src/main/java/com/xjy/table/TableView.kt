package com.xjy.table

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class TableView:ViewGroup {

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



    private val map = mutableMapOf<String, View>()
    private val resetMap = mutableMapOf<View, Size>()

    private val mTitleView by lazy {
        val tv = buildTextView()
        val lp = tv.layoutParams
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        tv
    }

    var dataSource = SimpleTableDataSource()

    init {
        reload()
        dataSource.configTitle(mTitleView)
    }

    private var resetSize = true

    fun reload(){
        resetMap.clear()
        resetSize = true
        removeAllViews()
        map.clear()
        var index = 0
        addView(mTitleView, index)
        mTitleView.text = dataSource.title()
        for (i in 0 until dataSource.numOfColumn()){
            index ++
            val tv = buildTextView()
            tv.text = dataSource.titleOfColumn(i)
            addView(tv, index)
        }
        for (row in 0 until dataSource.numOfRow()){
            for (column in 0 until dataSource.numOfColumn()){
                index ++
                val tv = buildTextView()
                addView(tv , index)
                val w = dataSource.widthOfCell(column)
                if (w != -1){
                    tv.layoutParams.width = w
                }
                dataSource.configCell(tv, row, column)

            }
        }
    }

    private fun buildTextView():TextView{
        val tv = TextView(context)
        tv.gravity = Gravity.CENTER
        tv.setBackgroundColor(Color.WHITE)
        val marign = MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        marign.topMargin = 1
        marign.leftMargin = 1
        marign.rightMargin = 1
        marign.bottomMargin = 1
        tv.layoutParams = marign
        return tv
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)


        // 计算出所有的childView的宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        // 记录如果是wrap_content是设置的宽和高

        var width = 0
        var height = 0

        val cCount = childCount

        var cWidth = 0
        var cHeight = 0
        var cParams :MarginLayoutParams? = null

        var allHeight = 0

        var allWidth = 0

        var columnIndex = 0

        val columnCount = dataSource.numOfColumn()

        var columnHeight = 0

        for (i in 0 until cCount){
            val childView = getChildAt(i)
            cWidth = childView.measuredWidth
            cHeight = childView.measuredHeight
            cParams = childView.layoutParams as MarginLayoutParams
            if (i == 0){
                //计算title的高度
                allHeight += cHeight + cParams.bottomMargin + cParams.topMargin
            }else {
                if (!cacheResetView.contains(childView)){
                    columnHeight = Math.max(cHeight + cParams.bottomMargin + cParams.topMargin, columnHeight)
                }
                columnIndex ++
                if (columnIndex == columnCount){
                    columnIndex = 0
                    allHeight += columnHeight
                    columnHeight = 0
                }
            }


        }

        width = allWidth
        height = allHeight

        setMeasuredDimension(
            if (widthMode == View.MeasureSpec.EXACTLY)
                sizeWidth
            else
                width, if (heightMode == View.MeasureSpec.EXACTLY)
                sizeHeight
            else
                height
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        map.clear()
        val cCount = childCount
        var columnCount = dataSource.numOfColumn()


        var cWidth = 0
        var cHeight = 0
        var cParams: ViewGroup.MarginLayoutParams? = null

        var columnIndex = 0
        var rowIndex = 0


        val titleView = getChildAt(0)
        cWidth = titleView.measuredWidth
        cHeight = titleView.measuredHeight
        cParams = titleView.layoutParams as MarginLayoutParams
        titleView.layout(cParams.leftMargin, cParams.topMargin, width - cParams.rightMargin, cHeight + cParams.bottomMargin)

        var ct = cParams.bottomMargin + cParams.topMargin + cHeight

        for (i in 1 until cCount step columnCount){
            var fillWidth = 0
            var fillCount = 0
            var rowHeight = 0
            var rowMargin = 0
            var realRowHeight = 0
            for (j in 0 until columnCount) {
                val childView = getChildAt(j + i)
                cParams = childView.layoutParams as MarginLayoutParams
                rowMargin += cParams.leftMargin + cParams.rightMargin
                val w = dataSource.widthOfCell(j)
                if (w == -1){
                    fillCount ++
                }else {
                    rowMargin += w
                }
                if (cacheResetView.contains(childView)){
                    continue
                }
                cHeight = childView.measuredHeight
                realRowHeight = Math.max(realRowHeight, cHeight)
                rowHeight = Math.max(rowHeight, cHeight + cParams.topMargin + cParams.bottomMargin)



            }
            fillWidth = (width - rowMargin) / fillCount
            var cl = 0
            var cr = 0
            var cb = 0

            for (j in 0 until columnCount){
                val childView = getChildAt(j + i)
                cParams = childView.layoutParams as MarginLayoutParams
                cl += cParams.leftMargin
                cb = ct + realRowHeight + cParams.topMargin
                val w = dataSource.widthOfCell(j)
                if (w == -1){
                    cr = cl + fillWidth
                } else {
                    cr = cl + w
                }
                childView.layout(cl, ct, cr, cb)
                if (rowIndex > 0) {
                    val cellId = dataSource.idOfCell(rowIndex - 1, j)
//                    Log.e("TAG", "$rowIndex-$j==$cellId")
                    if (map.containsKey(cellId)) {
                        val cacheView = map.get(cellId)!!
//                        cacheView.setBackgroundColor(Color.RED)
                        val expendWdith = if (w == -1) fillWidth else w
                        if (cacheView.right >= cr){
                            cacheView.layout(cacheView.left, cacheView.top, cacheView.right, cacheView.bottom + realRowHeight + cParams.bottomMargin + cParams.topMargin)
                        }else {
                            cacheView.layout(cacheView.left, cacheView.top, cacheView.right + expendWdith + cParams.leftMargin + cParams.rightMargin, cacheView.bottom)
                        }
                        childView.visibility = View.GONE
                        cacheResetView.add(cacheView)
                        if (resetMap.containsKey(cacheView)){
                            resetMap.remove(cacheView)
                        }
                        resetMap.put(cacheView, Size(cacheView.right - cacheView.left, cacheView.bottom - cacheView.top))
                    } else {
                        map.put(cellId, childView)

                    }
                }
                cl = cr + cParams.rightMargin

            }
            ct += rowHeight
            rowIndex ++

        }

        reset()
    }

    private val cacheResetView = mutableListOf<View>()

    private fun reset(){
        if (resetSize){
            resetSize = false
            for (key in resetMap.keys){
                val lp = key.layoutParams
                lp.width = resetMap.get(key)!!.width
                lp.height = resetMap.get(key)!!.height
                key.layoutParams = lp
            }
        }
    }


    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return ViewGroup.MarginLayoutParams(context, attrs)
    }
}