package com.example.litera.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PeminjamanAktif(
    var idBuku: String? = "",
    var judul: String? = "",
    var penulis: String? = "",
    var imageUrl: String? = "",
    var status: String? = "Tidak Tersedia", // Contoh: "Dipinjam" atau "Tersedia"
//    var rating: String? = "4.8"
) : Parcelable