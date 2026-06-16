package com.dna.litera.data.local

import androidx.room.*

@Dao
interface BukuCartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Jika buku sudah ada, akan di-update
    suspend fun tambahKeKeranjang(buku: BukuCart)

    @Delete
    suspend fun hapusDariKeranjang(buku: BukuCart)

    @Query("SELECT * FROM buku_cart ORDER BY tanggalDitambahkan DESC")
    suspend fun ambilSemuaKeranjang(): List<BukuCart>

    @Query("DELETE FROM buku_cart")
    suspend fun kosongkanKeranjang()
}