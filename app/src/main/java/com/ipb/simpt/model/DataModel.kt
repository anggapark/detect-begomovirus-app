package com.ipb.simpt.model

data class DataModel(

    var id: String = "",
    var komoditasId: String = "",
    var komoditasName: String = "",

    var penyakitId: String = "",
    var penyakitName: String = "",

    var kategoriPathogen: String = "",

    var pathogenId: String = "",
    var pathogenName: String = "",

    var gejalaId: String = "",
    var gejalaName: String = "",

    var dataset: String = "",
    var deskripsi: String = "",

    var uid: String = "",
    var userName: String = "",
    var userNim: String = "",

    var url: String = "",
    var timestamp: Long = 0,
    var status: String = ""

)