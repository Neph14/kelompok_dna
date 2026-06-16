package com.dna.litera.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Buku(
    val judul: String? = null,
    val jurusan: String? = null, // Tambahkan ini jika belum ada
    val status: String? = null,
    val tipe: String? = null,
    val penulis: String? = null,
    val imageUrl: String? = null,
    val deskripsi: String? = null
) : Parcelable