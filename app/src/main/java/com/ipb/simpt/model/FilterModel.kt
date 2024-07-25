package com.ipb.simpt.model

import android.os.Parcel
import android.os.Parcelable

data class FilterModel(
    val komoditasId: String? = null,
    val komoditas: String? = null,
    val penyakitId: String? = null,
    val penyakit: String? = null,
    val kategoriPathogen: String? = null,
    val pathogenId: String? = null,
    val pathogen: String? = null,
    val gejalaId: String? = null,
    val gejala: String? = null,
    val date: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(komoditas)
        parcel.writeString(penyakit)
        parcel.writeString(kategoriPathogen)
        parcel.writeString(pathogen)
        parcel.writeString(gejala)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FilterModel> {
        override fun createFromParcel(parcel: Parcel): FilterModel {
            return FilterModel(parcel)
        }

        override fun newArray(size: Int): Array<FilterModel?> {
            return arrayOfNulls(size)
        }
    }
}
