package com.ipb.simpt.data.model

import java.sql.Timestamp

data class  CategoryModel(

    // Must be same as in firebase
    var id: String = "",
    var category: String = "",
    var name: String = "",
    var timestamp: Long = 0,
    var uid: String = ""
)
