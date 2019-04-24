package com.xjy.tabledemo

data class NorHandlingDetail(
                             val list: List<TaskInfoList>)

data class TaskApply(val searchValue: String, val createTime: String, val id: String,
                     val applyNo: String, val unit: String, val title: String, val content: String, val status: String, val mode: String)

data class TaskInfoList(
                        val ordernumber: String, val send: String, val copy: String,
                        val sendList: ArrayList<SendList>, val copyList: ArrayList<CopyList>,
                        val status: String, val opinion: String, val isRead: String)

data class UpLoadBean(val id: String, val fileName: String, val saveName: String, val busNo: String, val fileType: String, val viewPath: String)

data class SendList(val send: String, val status: String, val isRead: String)

data class CopyList(val copyval: String, val copy: String, val isRead: String)