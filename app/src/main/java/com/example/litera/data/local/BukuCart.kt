package com.example.litera.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buku_cart")
data class BukuCart(
    @PrimaryKey
    val id_buku: String, // Menggunakan ID dokumen Firestore sebagai Primary Key agar tidak ganda
    val judul: String,
    val penulis: String,
    val imageUrl: String, // Untuk menampilkan gambar buku di item_cart.xml
    val tanggalDitambahkan: Long = System.currentTimeMillis()
)