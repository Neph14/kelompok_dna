package com.dna.litera.data

data class Peminjaman(
    val idPeminjaman: String = "",
    val npmMahasiswa: String = "",
    val namaMahasiswa: String = "",
    val prodi: String = "",
    val waktuPengambilan: String = "",
    val durasiHari: Int = 1,
    val buktiKtmUrl: String = "",
    val statusPeminjaman: String = "PENDING", // PENDING atau ACTIVE
    val tanggalMulai: Long? = null,           // Menggunakan timestamp milidetik
    val tanggalKembali: Long? = null,
    val daftarBuku: List<Map<String, String>> = emptyList() // Menampung id_buku dan judul
)