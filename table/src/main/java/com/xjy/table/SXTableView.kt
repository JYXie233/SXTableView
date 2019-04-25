package com.xjy.table

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView

open class SXTableView : ViewGroup {
    //数据源
    var dataSource: SXTableDataSource = SimpleTableDataSource()
    //内边距
    var borderWidth = Util.dip2px(context, 1)
    //外边距
    var outSideBorderWidth = Util.dip2px(context, 5)
    //文字方向
    var textGravity:Int = Gravity.CENTER

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



    private val mTitleView by lazy {
        val tv = buildTextView()
        val lp = tv.layoutParams
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        tv
    }


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



    fun addColumnClickListener(column: Int, listener: SXOnTableColumnClickListener) {
        columnClickListeners.add(SXTableColumnClick(column, listener))
    }

    fun reload() {
        removeAllViews()
        var viewIndex = 0
        addView(mTitleView, viewIndex)
        mTitleView.text = dataSource.title()
        dataSource.configTitle(mTitleView)
        viewIndex ++

        for (column in 0 until dataSource.numOfColumn()) {
            val tv = buildTextView()
            tv.text = dataSource.titleOfColumn(column)
            dataSource.configColumnHeader(tv, column)
            addView(tv, viewIndex)
            viewIndex ++
        }

        for (row in 0 until dataSource.numOfRow()) {
            for (column in 0 until dataSource.numOfColumn()) {
                val tv = buildTextView()
                addView(tv, viewIndex)
                tv.tag = row
                dataSource.configCell(tv, row, column)
                columnClickListeners.forEach {
                    if (it.column == column){
                        tv.setOnClickListener(it)
                    }
                }
                viewIndex++
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = View.MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(sizeWidth, sizeHeight)

        measureTitle(widthMeasureSpec, heightMeasureSpec)

        var allHeight = mTitleView.measuredHeight + outSideBorderWidth * 2

        val columnHeaderHeight = measureColumnHeader(widthMeasureSpec, heightMeasureSpec)

        allHeight += columnHeaderHeight

        for (row in 0 until dataSource.numOfRow()){
            allHeight += measureRow(row, widthMeasureSpec, heightMeasureSpec) + borderWidth
        }

        for (row in 0 until dataSource.numOfRow()){
            measureRowFix(row, widthMeasureSpec, heightMeasureSpec)
        }

        setMeasuredDimension(sizeWidth, allHeight)

    }

    private fun measureTitle(widthMeasureSpec: Int, heightMeasureSpec: Int){
        val childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
            paddingLeft + paddingRight + outSideBorderWidth * 2, LayoutParams.MATCH_PARENT)
        val childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
            paddingTop + paddingBottom, LayoutParams.WRAP_CONTENT)
        mTitleView.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    private fun measureColumnHeader(widthMeasureSpec: Int, heightMeasureSpec: Int):Int{
        var childWidthMeasureSpec = 0
        var childHeightMeasureSpec = 0
        var lp = mTitleView.layoutParams
        var viewIndex = 1
        var unMeasureViews = mutableListOf<View>()
        var allExactlyWidth = 0//自定义的宽度
        var rowViews = mutableListOf<View>()
        for (columnIndex in 0 until dataSource.numOfColumn()){
            val child = getChildAt(viewIndex)
            lp = child.layoutParams
            val cWidth = dataSource.widthOfCell(columnIndex)
            if (cWidth == -1){
                unMeasureViews.add(child)
            } else {
                childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, borderWidth, cWidth)
                childHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, borderWidth, lp.height)
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                allExactlyWidth += child.measuredWidth
            }
            rowViews.add(child)
            viewIndex ++
        }
        val canDistributionWidth = measuredWidth - outSideBorderWidth * 2 - borderWidth * (dataSource.numOfColumn() - 1) - allExactlyWidth
        val childFillModeWidth = canDistributionWidth / unMeasureViews.count()

        unMeasureViews.forEach {
            lp = it.layoutParams
            childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, borderWidth, childFillModeWidth)
            childHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, borderWidth, lp.height)
            it.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }
        var columnHeight = 0
        rowViews.forEach {
            columnHeight = Math.max(it.measuredHeight, columnHeight)
        }

        rowViews.forEach {
            childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, it.measuredWidth)
            childHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, columnHeight)
            it.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }
        return columnHeight
    }

    private fun measureRowFix(row: Int, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val viewIndex = (row + 1) * dataSource.numOfColumn() + 1
        val rowCount = dataSource.numOfRow()
        for (columnIndex in 0 until dataSource.numOfColumn()) {
            val currentId = dataSource.idOfCell(row, columnIndex)
            val child = getChildAt(viewIndex + columnIndex)
            if (isTheFirstSameRow(row, columnIndex, child)) {
                var allSameRowHeight = 0
                for (r in row until rowCount) {
                    val nextRowId = dataSource.idOfCell(r, columnIndex)
                    if (nextRowId == currentId) {
                        val rowHeight = getRowHeight(r)
                        allSameRowHeight += rowHeight + borderWidth
                    } else {
                        break
                    }
                }
                val childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, child.measuredWidth)
                val childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, allSameRowHeight - borderWidth)
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            }
        }
    }

    private fun measureRow(row: Int, widthMeasureSpec: Int, heightMeasureSpec: Int):Int{
        var childWidthMeasureSpec = 0
        var childHeightMeasureSpec = 0
        var lp = mTitleView.layoutParams
        val viewIndexVal = (row + 1) * dataSource.numOfColumn() + 1
        var viewIndex = (row + 1) * dataSource.numOfColumn() + 1
        var unMeasureViews = mutableListOf<View>()
        var allExactlyWidth = 0//自定义的宽度
        var rowViews = mutableListOf<View>()
        val columnCount = dataSource.numOfColumn()
        for (columnIndex in 0 until columnCount){
            val child = getChildAt(viewIndex)
            lp = child.layoutParams
            val cWidth = dataSource.widthOfCell(columnIndex)
            if (cWidth == -1){
                unMeasureViews.add(child)
            } else {
                childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, borderWidth, cWidth)
                childHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, borderWidth, lp.height)
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                allExactlyWidth += child.measuredWidth
            }
            rowViews.add(child)
            viewIndex ++
        }
        val canDistributionWidth = measuredWidth - outSideBorderWidth * 2 - borderWidth * (dataSource.numOfColumn() - 1) - allExactlyWidth
        val childFillModeWidth = canDistributionWidth / unMeasureViews.count()

        unMeasureViews.forEach {
            lp = it.layoutParams
            childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, borderWidth, childFillModeWidth)
            childHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, borderWidth, lp.height)
            it.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }
        //处理横向合并
        var columnIndex = 0
        var lastId = ""
        rowViews.forEach {child ->
            val currentId = dataSource.idOfCell(row, columnIndex)
            if (columnIndex > 0){
                if (lastId == currentId){
                    child.visibility = View.GONE
                }
            }
            if (child.visibility != View.GONE) {

            }
            resetCellWidth(currentId, viewIndexVal, child, columnIndex, row, widthMeasureSpec, heightMeasureSpec)
            lastId = currentId
            columnIndex ++
        }

        var columnHeight = getRowHeight(row)
        columnIndex = 0

        rowViews.forEach {
            childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, it.measuredWidth)
            childHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, columnHeight)
            it.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }



        return columnHeight
    }

    private fun isTheFirstSameRow(row: Int, columnIndex:Int, view:View? = null):Boolean{
        val currentId = dataSource.idOfCell(row, columnIndex)
        val rowCount = dataSource.numOfRow()
        var isFirst = row == 0
        if (row > 0){
            val lastRowId = dataSource.idOfCell(row - 1, columnIndex)
            isFirst = currentId != lastRowId
        }
        var hasNextSame = false
        if (isFirst){
            if (row < rowCount - 1){
                val nextRowId = dataSource.idOfCell(row + 1, columnIndex)
                if (currentId == nextRowId){
//                    it.visibility = View.GONE
                    hasNextSame = true
                }
            }
        } else {
            view?.visibility = View.GONE
        }
        return hasNextSame && isFirst
    }

    private fun getRowHeight(row: Int):Int{
        var columnHeight = 0
        val columnCount = dataSource.numOfColumn()
        for (columnIndex in 0 until columnCount) {
            val currentId = dataSource.idOfCell(row, columnIndex)
            var needCountHeight = !isTheFirstSameRow(row, columnIndex)
            if (needCountHeight) {
                if (row > 0) {
                    val lastRowId = dataSource.idOfCell(row - 1, columnIndex)
                    if (currentId == lastRowId) {
                        needCountHeight = false
                    }
                }
            }
            if (needCountHeight) {
                val index = (row + 1) * columnCount + 1 + columnIndex
                val child = getChildAt(index)
                columnHeight = Math.max(child.measuredHeight, columnHeight)
            }
        }
        return columnHeight
    }

    private fun resetCellWidth(currentId:String, viewIndex:Int, child:View, columnIndex:Int, row:Int, widthMeasureSpec: Int, heightMeasureSpec: Int){
        if (row == 2){
            Log.d("", "")
        }
        val columnCount = dataSource.numOfColumn()
        if (columnIndex < columnCount - 1){
            for (column in columnIndex + 1 until columnCount){
                val otherId = dataSource.idOfCell(row, column)
                if (currentId == otherId){
                    val otherView = getChildAt(viewIndex + column)
                    val childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, child.measuredWidth + borderWidth + otherView.measuredWidth)
                    val childHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, child.measuredHeight)
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                }else {
                    break
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var cl = outSideBorderWidth
        var cr = 0
        var ct = outSideBorderWidth
        var cb = 0
        cr = cl + mTitleView.measuredWidth
        cb = ct + mTitleView.measuredHeight
        mTitleView.layout(cl, ct, cr, cb)
        var viewIndex = 1
        cl = outSideBorderWidth
        ct = cb + borderWidth
        for (column in 0 until dataSource.numOfColumn()){
            val child = getChildAt(viewIndex)
            cr = cl + child.measuredWidth
            cb = ct + child.measuredHeight
            child.layout(cl, ct, cr, cb)
            viewIndex ++
            cl = cr + borderWidth
        }
        for (row in 0 until dataSource.numOfRow()){
            ct = cb + borderWidth
            cl = outSideBorderWidth
            var rowHeight = 0
            for (column in 0 until dataSource.numOfColumn()){
                val child = getChildAt(viewIndex)
                if (child.visibility != View.GONE) {
                    cr = cl + child.measuredWidth
                    cb = ct + child.measuredHeight
                    rowHeight = child.measuredHeight
                    child.layout(cl, ct, cr, cb)
                    cl = cr + borderWidth
                }else {
                    val currentId = dataSource.idOfCell(row, column)

                    if (row > 0){
                        val lastRowId = dataSource.idOfCell(row - 1, column)
                        if (lastRowId == currentId){
                            cr = cl + child.measuredWidth
                            cl = cr + borderWidth
                        }
                    }
                }
                viewIndex++
            }
            cb = ct + rowHeight
        }
    }

    private fun buildTextView(): TextView {
        val tv = TextView(context)
        tv.gravity = textGravity
        tv.setBackgroundColor(Color.WHITE)
        tv.layoutParams = MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        return tv
    }


    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return ViewGroup.MarginLayoutParams(context, attrs)
    }
}