package com.dna.litera.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BukuMilikSaya(
    var idBuku: String? = "",
    var judul: String? = "",
    var penulis: String? = "",
    var imageUrl: String? = "",
    var statusPeminjaman: String? = "", // Tambahan field status untuk kontrol timer
    var tanggalKembali: Long? = 0L
) : Parcelable