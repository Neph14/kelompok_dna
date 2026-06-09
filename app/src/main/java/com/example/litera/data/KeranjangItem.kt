package com.example.litera.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class KeranjangItem(
    var idBuku: String = "",
    var judul: String = "",
    var penulis: String = "",
    var imageUrl: String = "",
    var jumlahPinjam: Int = 1
) : Parcelable