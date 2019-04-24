package com.xjy.tabledemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.xjy.table.SXOnTableColumnClickListener
import com.xjy.table.SXTableView

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val table = findViewById<SXTable>(R.id.table)

        table.addColumnClickListener(2, object : SXOnTableColumnClickListener {
            override fun onColumnClick(row: Int) {

                val model = table.getRowData(row)
                Log.d("Click", "$row:${model.ordernumber}")
            }
        })
        val list = mutableListOf<TaskInfoList>()
        for (i in 0..5){
            list.add(build(i))
        }
        val model = NorHandlingDetail(list = list)
        table.setModel(model)
    }

    fun build(order: Int): TaskInfoList {
        val l = arrayListOf<SendList>()
        for (i in 0..order){
            l.add(SendList(send = "$order 黄", status = "s", isRead = "r"))
        }
        return TaskInfoList(
            copy = "查看更多", ordernumber = "$order",
            opinion = "asd", copyList = arrayListOf(), status = "du", isRead = "yue",
            send = "h",
            sendList = l
        )
    }
}
