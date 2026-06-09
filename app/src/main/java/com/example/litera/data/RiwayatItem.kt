package com.example.litera.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RiwayatItem(
    var idTransaksi: String = "",
    var npm: String = "",
    var judulBuku: String = "",
    var tanggalPinjam: String = "",
    var tanggalKembali: String = "",
    var statusPinjam: String = "" // Contoh nilai: "Dipinjam" atau "Dikembalikan"
) : Parcelable