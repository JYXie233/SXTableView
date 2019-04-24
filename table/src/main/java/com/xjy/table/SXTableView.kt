package com.xjy.table

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView

open class SXTableView : ViewGroup {

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

    private val columnClickListeners = mutableListOf<SXTableColumnClick>()

    private val map = mutableMapOf<String, View>()
    private val resetMap = mutableMapOf<View, Size>()

    private val mTitleView by lazy {
        val tv = buildTextView()
        val lp = tv.layoutParams
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        tv
    }

    var dataSource: SXTableDataSource = SimpleTableDataSource()
    var borderWidth = Util.dip2px(context, 1)

    private var fillColumnWidth = 0

    init {

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onGlobalLayout() {
                reload()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }

        })
    }

    private var resetSize = true


    fun addColumnClickListener(column: Int, listener: SXOnTableColumnClickListener) {
        columnClickListeners.add(SXTableColumnClick(column, listener))
    }

    private fun realReload() {
        resetMap.clear()
        resetSize = true
        removeAllViews()
        map.clear()
        var index = 0
        addView(mTitleView, index)
        mTitleView.text = dataSource.title()
        dataSource.configTitle(mTitleView)
        for (i in 0 until dataSource.numOfColumn()) {
            index++
            val tv = buildTextView()
            tv.text = dataSource.titleOfColumn(i)
            val lp = tv.layoutParams
            val w = dataSource.widthOfCell(i)
            if (w != -1) {
                lp.width = w
            } else {
                lp.width = fillColumnWidth
            }
            tv.layoutParams = lp
            dataSource.configColumnHeader(tv, i)
            addView(tv, index)
        }
        for (row in 0 until dataSource.numOfRow()) {
            for (column in 0 until dataSource.numOfColumn()) {
                index++
                val tv = buildTextView()

                val lp = tv.layoutParams
                val w = dataSource.widthOfCell(column)
                if (w != -1) {
                    lp.width = w
                } else {
                    lp.width = fillColumnWidth
                }
                tv.layoutParams = lp
                dataSource.configCell(tv, row, column)
                columnClickListeners.forEach {
                    if (it.cloumn == column) {
                        tv.tag = row
                        tv.setOnClickListener(it)
                    }
                }
                addView(tv, index)

            }
        }
    }

    fun reload() {
        resetMap.clear()
        resetSize = true
        removeAllViews()
        map.clear()

        var index = 0
        addView(mTitleView, index)
        mTitleView.text = dataSource.title()
        dataSource.configTitle(mTitleView)
        for (i in 0 until dataSource.numOfColumn()) {
            index++
            val tv = buildTextView()
            tv.text = dataSource.titleOfColumn(i)
            val lp = tv.layoutParams
            val w = dataSource.widthOfCell(i)
            if (w != -1) {
                lp.width = w
            } else {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            tv.layoutParams = lp
            addView(tv, index)
        }
        calFill()
        realReload()
    }

    private fun buildTextView(): TextView {
        val tv = TextView(context)
        tv.gravity = Gravity.CENTER
        tv.setBackgroundColor(Color.WHITE)
        val marign = MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        marign.topMargin = borderWidth
        marign.leftMargin = borderWidth
        marign.rightMargin = borderWidth
        marign.bottomMargin = borderWidth
        tv.layoutParams = marign
        return tv
    }

    private fun calFill() {

        var fillCount = 0
        var knowUseWidth = 0
        for (column in 0 until dataSource.numOfColumn()) {
            val w = dataSource.widthOfCell(column)
            if (w == -1) {
                fillCount++
            } else {
                knowUseWidth += w //getChildAt(1 + column).measuredWidth
            }
        }
        knowUseWidth += (dataSource.numOfColumn() + 1) * borderWidth
        fillColumnWidth = (width - knowUseWidth) / fillCount

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
        var cParams: MarginLayoutParams? = null

        var allHeight = 0

        var allWidth = 0

        var columnIndex = 0

        val columnCount = dataSource.numOfColumn()

        var columnHeight = 0

        for (i in 0 until cCount) {
            val childView = getChildAt(i)
            cWidth = childView.measuredWidth
            cHeight = childView.measuredHeight
            if (i == 0) {
                //计算title的高度
                allHeight += cHeight + borderWidth
            } else {
                if (!cacheResetView.contains(childView)) {
                    columnHeight = Math.max(cHeight + borderWidth, columnHeight)
                }
                columnIndex++
                if (columnIndex == columnCount) {
                    columnIndex = 0
                    allHeight += columnHeight
                    columnHeight = 0
                }
            }
        }

        width = allWidth
        height = allHeight + borderWidth

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

        var rowIndex = 0
        if (cCount > 0) {
            val titleView = getChildAt(0)
            cWidth = titleView.measuredWidth
            cHeight = titleView.measuredHeight
            titleView.layout(borderWidth, borderWidth, width - borderWidth, cHeight + borderWidth)
        }
        var ct = borderWidth + borderWidth + cHeight

        for (i in 1 until cCount step columnCount) {
            var rowHeight = 0

            var realRowHeight = 0
            for (j in 0 until columnCount) {
                val childView = getChildAt(j + i)
                if (cacheResetView.contains(childView)) {
                    continue
                }
                cHeight = childView.measuredHeight
                realRowHeight = Math.max(realRowHeight, cHeight)
                rowHeight = Math.max(rowHeight, cHeight + borderWidth)
            }

            var cl = 0
            var cr = 0
            var cb = 0

            for (j in 0 until columnCount) {
                val childView = getChildAt(j + i)
                cWidth = childView.measuredWidth
                cl += borderWidth
                cb = ct + realRowHeight
                val w = dataSource.widthOfCell(j)
                if (w == -1) {
                    cr = cl + fillColumnWidth
                } else {
                    cr = cl + cWidth
                }

                childView.layout(cl, ct, cr, cb)
                if (rowIndex > 0) {
                    val cellId = dataSource.idOfCell(rowIndex - 1, j)
//                    Log.e("TAG", "$rowIndex-$j==$cellId")
                    if (map.containsKey(cellId)) {
                        val cacheView = map.get(cellId)!!
//                        cacheView.setBackgroundColor(Color.RED)
                        val expendWdith = if (w == -1) fillColumnWidth else cWidth
                        if (cacheView.right >= cr) {
                            cacheView.layout(
                                cacheView.left,
                                cacheView.top,
                                cacheView.right,
                                cacheView.bottom + realRowHeight + borderWidth
                            )
                        } else {
                            cacheView.layout(
                                cacheView.left,
                                cacheView.top,
                                cacheView.right + expendWdith + borderWidth,
                                cacheView.bottom
                            )
                        }
                        childView.visibility = View.GONE
                        cacheResetView.add(cacheView)
                        if (resetMap.containsKey(cacheView)) {
                            resetMap.remove(cacheView)
                        }
                        resetMap.put(
                            cacheView,
                            Size(cacheView.right - cacheView.left, cacheView.bottom - cacheView.top)
                        )
                    } else {
                        map.put(cellId, childView)

                    }
                }
                cl = cr //- borderWidth

            }
            ct += rowHeight
            rowIndex++

        }

        reset()
    }

    private val cacheResetView = mutableListOf<View>()

    private fun reset() {
        if (resetSize) {
            resetSize = false
            for (key in resetMap.keys) {
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