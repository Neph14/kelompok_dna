package com.example.litera.buku

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.litera.data.BukuMilikSaya
import com.example.litera.databinding.FragmentBukuListBinding
import com.google.firebase.firestore.FirebaseFirestore

class BukuListFragment : Fragment() {

    private var _binding: FragmentBukuListBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var bukuSayaAdapter: BukuSayaAdapter
    private var listBukuPinjaman = ArrayList<BukuMilikSaya>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBukuListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        ambilDataPeminjamanAktif()
    }

    private fun setupRecyclerView() {
        bukuSayaAdapter = BukuSayaAdapter(listBukuPinjaman, false) { bukuTerpilih ->
            // AKSI TOMBOL PERPANJANG DIKLIK
            Toast.makeText(context, "Buku '${bukuTerpilih.judul}' akan dimasukkan ke keranjang perpanjangan!", Toast.LENGTH_SHORT).show()
            // TODO: Tambahkan ke fungsi keranjang di langkah berikutnya setelah modul ini kelar
        }
        binding.rvBuku.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBuku.adapter = bukuSayaAdapter
    }

    private fun ambilDataPeminjamanAktif() {
        db.collection("peminjaman_buku")
            .whereEqualTo("statusPeminjaman", "ACTIVE") // Cari transaksi aktif
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(context, "Gagal memuat: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val listHasilTerfilter = ArrayList<BukuMilikSaya>()

                // Cek apakah ada dokumen transaksi yang berstatus ACTIVE
                if (value != null && !value.isEmpty) {
                    value.documents.forEach { dokumen ->
                        val tglKembali = dokumen.getLong("tanggalKembali") ?: 0L
                        val daftarBukuRaw = dokumen.get("daftarBuku") as? List<Map<String, Any>>

                        daftarBukuRaw?.forEach { mapBuku ->
                            val bukuMilik = BukuMilikSaya(
                                idBuku = mapBuku["idBuku"] as? String ?: "",
                                judul = mapBuku["judul"] as? String ?: "Tanpa Judul",
                                penulis = mapBuku["penulis"] as? String ?: "Anonim",
                                imageUrl = mapBuku["imageUrl"] as? String ?: "",
                                tanggalKembali = tglKembali
                            )
                            listHasilTerfilter.add(bukuMilik)
                        }
                    }

                    listBukuPinjaman.clear()
                    listBukuPinjaman.addAll(listHasilTerfilter)
                    bukuSayaAdapter.updateData(listBukuPinjaman)
                } else {
                    // FALLBACK: Jika tidak ada peminjaman ACTIVE, ambil data default dari koleksi_buku agar tidak blank
                    ambilDataKatalogDefault()
                }
            }
    }

    // Fungsi tambahan untuk mengambil data langsung dari koleksi_buku jika data transaksi kosong
    private fun ambilDataKatalogDefault() {
        db.collection("koleksi_buku").limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val listDefault = ArrayList<BukuMilikSaya>()
                for (document in documents) {
                    // Kita set simulasi tanggalKembali = waktu sekarang + 2 hari agar statusnya memicu "Sisa X Hari"
                    val simulasiDuaHari = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000)

                    val bukuMilik = BukuMilikSaya(
                        idBuku = document.id,
                        judul = document.getString("judul") ?: "Tanpa Judul",
                        penulis = document.getString("penulis") ?: "Anonim",
                        imageUrl = document.getString("imageUrl") ?: "",
                        tanggalKembali = simulasiDuaHari // Beri waktu simulasi biar tombol perpanjang terkunci
                    )
                    listDefault.add(bukuMilik)
                }
                listBukuPinjaman.clear()
                listBukuPinjaman.addAll(listDefault)
                bukuSayaAdapter.updateData(listBukuPinjaman)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Katalog default gagal dimuat", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}