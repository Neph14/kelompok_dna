package com.dna.litera.buku

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.dna.litera.R
import com.dna.litera.data.Buku
import com.dna.litera.data.local.AppDatabase
import com.dna.litera.data.local.BukuCart
import com.dna.litera.databinding.FragmentDetailBukuBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailBukuActivity : AppCompatActivity() {

    private lateinit var binding: FragmentDetailBukuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentDetailBukuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Tangkap objek data Buku yang dikirim dari HomeFragment
        val buku = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_BUKU", Buku::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_BUKU") as? Buku
        }

        // 2. Tampilkan data ke komponen UI XML secara dinamis
        buku?.let { dataBuku ->
            binding.tvJudulDetail.text = dataBuku.judul
            binding.tvPenulisDetail.text = dataBuku.penulis
            binding.tvDeskripsiDetail.text = dataBuku.deskripsi
            binding.tvKategoriDetail.text = "Kategori : ${dataBuku.tipe}"

            // DIUBAH: Load gambar cover buku menggunakan Glide + Palette API untuk warna dinamis
            Glide.with(this)
                .asBitmap()
                .load(dataBuku.imageUrl)
                .placeholder(R.drawable.placeholder_buku)
                .error(R.drawable.placeholder_buku)
                .into(object : BitmapImageViewTarget(binding.ivCoverDetail) {
                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                        super.onResourceReady(bitmap, transition)

                        // Ekstrak warna dominan yang soft/kalem dari cover buku
                        Palette.from(bitmap).generate { palette ->
                            if (palette != null) {
                                // Fallback jika gagal ekstrak warna (Abu pudar default awal)
                                val defaultColor = Color.parseColor("#F0F0F0")

                                // Ambil warna light muted (atau light vibrant jika ingin warna yang lebih cerah)
                                val extractedColor = palette.getLightMutedColor(defaultColor)

                                // Terapkan warna ke view background yang membungkus cover & judul teks
                                binding.viewBackgroundColor.setBackgroundColor(extractedColor)
                            }
                        }
                    }
                })
        }

        // 3. Tombol Kembali
        binding.btnBackDetail.setOnClickListener {
            finish()
        }

        // 4. Tombol Tambah ke Daftar Pinjam
        binding.btnTambahDaftarPinjam.setOnClickListener {
            buku?.let { dataBuku ->
                tambahBukuKeKeranjangLokal(dataBuku)
            }
        }
    }

    private fun tambahBukuKeKeranjangLokal(buku: Buku) {
        // 1. Cek status ketersediaan buku terlebih dahulu
        if (!buku.status.equals("Tersedia", ignoreCase = true)) {
            Toast.makeText(this, "Buku nya lagi kosong, coba lain waktu", Toast.LENGTH_SHORT).show()
            return
        }

        val database = AppDatabase.getDatabase(this)
        val cartDao = database.bukuCartDao()

        // Buat objek BukuCart (Room Entity)
        val itemBaru = BukuCart(
            id_buku = buku.judul ?: "", // Gunakan Judul sebagai ID sementara jika ID Firestore belum diteruskan
            judul = buku.judul ?: "Tanpa Judul",
            penulis = buku.penulis ?: "Anonim",
            imageUrl = buku.imageUrl ?: ""
        )

        lifecycleScope.launch(Dispatchers.IO) {
            cartDao.tambahKeKeranjang(itemBaru)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@DetailBukuActivity, "Berhasil masuk ke daftar pinjam!", Toast.LENGTH_SHORT).show()
                finish() // Tutup halaman detail setelah berhasil tambah
            }
        }
    }
}