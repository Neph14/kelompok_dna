package com.dna.litera.pinjam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dna.litera.data.local.AppDatabase
import com.dna.litera.data.local.BukuCart
import com.dna.litera.data.local.BukuCartDao
import com.dna.litera.databinding.ActivityCartBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var database: AppDatabase
    private lateinit var cartDao: BukuCartDao
    private lateinit var cartAdapter: BukuCartAdapter
    private var npmUserSession: String = "24111019"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // Ambil NPM dari sesi
        val sharedPref = getSharedPreferences("SesiLitera", MODE_PRIVATE)
        npmUserSession = sharedPref.getString("LOGIN_NPM", "24111019") ?: "24111019"

        // Inisialisasi Room Database lokal
        database = AppDatabase.getDatabase(this)
        cartDao = database.bukuCartDao()

        // Tampilkan data user
        ambilDataUserOtomatis()

        // 1. Aksi Tombol Kembali (Back) di Toolbar
        binding.btnBackCart.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 2. Setup RecyclerView dan Adapter Keranjang
        setupRecyclerView()

        // Ambil data dari database lokal saat activity dibuka
        muatDataKeranjang()

        // 3. Klik tombol panel atas "Data Peminjam >" untuk isi form
        binding.btnMenujuFormDataPeminjam.setOnClickListener {
            val intent = Intent(this, FormPinjamActivity::class.java)
            startActivity(intent)
        }

    // 4. Klik tombol "Pinjam" utama di bagian paling bawah
        binding.btnPinjamBukuUtama.setOnClickListener {
            validasiDanPinjam()
        }
    }

    private fun validasiDanPinjam() {
        // Tampilkan Loading
        val progressDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage("Sedang mengecek stok buku...")
            .setCancelable(false)
            .show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val listBukuCart = cartDao.ambilSemuaKeranjang()

                if (listBukuCart.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        Toast.makeText(this@CartActivity, "Tambahkan dulu buku yang ingin dipinjam", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Cek stok buku di Firestore
                val firestore = FirebaseFirestore.getInstance()
                val listBukuKosong = mutableListOf<String>()

                for (buku in listBukuCart) {
                    // Kita cari berdasarkan judul
                    val result = firestore.collection("koleksi_buku")
                        .whereEqualTo("judul", buku.judul)
                        .get().await()

                    if (!result.isEmpty) {
                        val doc = result.documents[0]
                        val status = doc.getString("status") ?: ""
                        if (!status.equals("Tersedia", ignoreCase = true)) {
                            listBukuKosong.add(buku.judul)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if (listBukuKosong.isNotEmpty()) {
                        val pesan = "Buku ini lagi kosong: ${listBukuKosong.joinToString(", ")}, yakin anda akan lanjut pinjam?"
                        androidx.appcompat.app.AlertDialog.Builder(this@CartActivity)
                            .setTitle("Stok Tidak Tersedia")
                            .setMessage(pesan)
                            .setPositiveButton("Lanjut Pinjam") { _, _ ->
                                eksekusiPeminjamanKeFirestore()
                            }
                            .setNegativeButton("Pikir-pikir dulu", null)
                            .show()
                    } else {
                        eksekusiPeminjamanKeFirestore()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@CartActivity, "Gagal mengecek stok: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("CartActivity", "Error validation: ", e)
                }
            }
        }
    }

    private fun ambilDataUserOtomatis() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(npmUserSession).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nama = document.getString("name") ?: document.getString("nama") ?: "Nama Tidak Ditemukan"
                    binding.tvNamaPeminjamCart.text = nama
                    binding.tvNPMFormatterCart.text = npmUserSession
                }
            }
    }

    private fun setupRecyclerView() {
        binding.rvCart.layoutManager = LinearLayoutManager(this)

        // Inisialisasi adapter dengan list kosong, dan berikan aksi hapus buku
        cartAdapter = BukuCartAdapter(emptyList()) { bukuDicari ->
            hapusBukuDariKeranjang(bukuDicari)
        }
        binding.rvCart.adapter = cartAdapter
    }

    private fun muatDataKeranjang() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listKeranjang = cartDao.ambilSemuaKeranjang()
            withContext(Dispatchers.Main) {
                // Update adapter dengan data terbaru dari Room
                cartAdapter.updateData(listKeranjang)
            }
        }
    }

    private fun hapusBukuDariKeranjang(buku: BukuCart) {
        lifecycleScope.launch(Dispatchers.IO) {
            cartDao.hapusDariKeranjang(buku)
            val listTerbaru = cartDao.ambilSemuaKeranjang()
            withContext(Dispatchers.Main) {
                cartAdapter.updateData(listTerbaru)
                Toast.makeText(this@CartActivity, "${buku.judul} dihapus dari keranjang", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eksekusiPeminjamanKeFirestore() {
        val progressDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage("Sedang memproses peminjaman...")
            .setCancelable(false)
            .show()

        val firestore = FirebaseFirestore.getInstance()

        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Ambil semua buku yang ada di dalam keranjang lokal Room
            val listBukuCart = cartDao.ambilSemuaKeranjang()

            if (listBukuCart.isEmpty()) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@CartActivity, "Keranjang kamu masih kosong!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // 2. Tarik data profil lengkap mahasiswa dari koleksi 'users' berdasarkan NPM
            firestore.collection("users").document(npmUserSession).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val namaMhs = document.getString("name") ?: document.getString("nama") ?: "Nama Tidak Ditemukan"
                        val jurusanMhs = document.getString("jurusan") ?: "Sistem Informasi"

                        // 3. Konversi listBukuCart
                        val listBukuMap = listBukuCart.map { buku ->
                            mapOf(
                                "id_buku" to buku.id_buku,
                                "judul" to buku.judul,
                                "penulis" to buku.penulis,
                                "imageUrl" to buku.imageUrl
                            )
                        }

                        // 4. Bungkus data transaksi
                        val waktuSekarang = System.currentTimeMillis()
                        val durasiPinjamMili = 24 * 60 * 60 * 1000L // 24 Jam
                        val tanggalKembaliMili = waktuSekarang + durasiPinjamMili

                        val dataPeminjaman = mapOf(
                            "npm" to npmUserSession,
                            "name" to namaMhs,
                            "jurusan" to jurusanMhs,
                            "status_peminjaman" to "MENUNGGU_KONFIRMASI",
                            "tanggal_pengajuan" to Timestamp.now(),
                            "tanggal_kembali" to tanggalKembaliMili, // Set batas waktu 24 jam
                            "tanggal_konfirmasi_admin" to null,
                            "buku_dipinjam" to listBukuMap
                        )

                        // 5. Kirim ke koleksi 'peminjaman'
                        firestore.collection("peminjaman").add(dataPeminjaman)
                            .addOnSuccessListener {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    cartDao.kosongkanKeranjang()
                                    withContext(Dispatchers.Main) {
                                        progressDialog.dismiss()
                                        tampilkanDialogSukses()
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                progressDialog.dismiss()
                                Toast.makeText(this@CartActivity, "Gagal ke Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this@CartActivity, "Data user tidak ditemukan!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this@CartActivity, "Gagal mengambil data user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Fungsi pembantu untuk merapikan kode dialog pop-up kamu
    private fun tampilkanDialogSukses() {
        androidx.appcompat.app.AlertDialog.Builder(this@CartActivity)
            .setTitle("Peminjaman Berhasil Dibuat")
            .setMessage("Silahkan ambil buku nya ke perpustakaan dan perlihatkan KTM anda pada saat meminjam.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(this@CartActivity, com.dna.litera.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            }
            .show()
    }
}