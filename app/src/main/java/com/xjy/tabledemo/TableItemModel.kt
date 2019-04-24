package com.xjy.tabledemo

data class TableItemModel(val ordernumber: String,
                          val copy:String,
                          val status: String,
                          val isRead: String,
                          val copyList: List<CopyList>)