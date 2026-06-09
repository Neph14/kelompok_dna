package com.example.litera.pinjam

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.litera.R
import com.example.litera.data.KeranjangItem
import com.example.litera.databinding.FragmentDaftarPinjamBinding

class DaftarPinjamFragment : Fragment(R.layout.fragment_daftar_pinjam) {

    private lateinit var binding: FragmentDaftarPinjamBinding
    private lateinit var keranjangAdapter: KeranjangAdapter

    // Menggunakan KeranjangItem sesuai dengan cetakan yang diminta oleh KeranjangAdapter
    private val dummyKeranjang = mutableListOf<KeranjangItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDaftarPinjamBinding.bind(view)

        // 1. Isi data dummy menggunakan struktur KeranjangItem
        if (dummyKeranjang.isEmpty()) {
            dummyKeranjang.add(KeranjangItem(
                judul = "Statistika dan Probabilitas",
                penulis = "Dr. Boediono",
                jumlahPinjam = 1,
                imageUrl = ""
            ))
            dummyKeranjang.add(KeranjangItem(
                judul = "Hujan",
                penulis = "Tere Liye",
                jumlahPinjam = 1,
                imageUrl = ""
            ))
            dummyKeranjang.add(KeranjangItem(
                judul = "Akuntansi Sektor Publik",
                penulis = "Indra Bastian",
                jumlahPinjam = 1,
                imageUrl = ""
            ))
        }

        // 2. Setup RecyclerView LayoutManager
        binding.rvKeranjangPinjam.layoutManager = LinearLayoutManager(context)

        // 3. Inisialisasi KeranjangAdapter dengan menyuplai 3 parameter sesuai kontrak barunya
        keranjangAdapter = KeranjangAdapter(
            listKeranjang = dummyKeranjang,
            onJumlahChanged = {
                // Dipicu saat tombol + / - diklik atau checkbox dicentang/dilepas
                updateRingkasanTeks(keranjangAdapter.hitungTotalTerpilih())
            },
            onHapusItem = { posisi ->
                // Dipicu saat ikon tong sampah diklik
                if (posisi in dummyKeranjang.indices) {
                    dummyKeranjang.removeAt(posisi)
                    keranjangAdapter.notifyItemRemoved(posisi)
                    keranjangAdapter.notifyItemRangeChanged(posisi, dummyKeranjang.size)
                    updateRingkasanTeks(keranjangAdapter.hitungTotalTerpilih())
                }
            }
        )
        binding.rvKeranjangPinjam.adapter = keranjangAdapter

        // Set status teks jumlah buku terpilih pertama kali saat halaman dimuat
        updateRingkasanTeks(keranjangAdapter.hitungTotalTerpilih())

        // 4. Logika Checkbox "Pilih Semua" di sticky bar bawah
        binding.cbPilihSemua.setOnCheckedChangeListener { _, isChecked ->
            keranjangAdapter.pilihSemuaItem(isChecked)
        }

        // 5. Tombol Kembali
        binding.btnBackKeranjang.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 6. Tombol Konfirmasi Pinjam untuk masuk ke Form Pengajuan
        // 6. Tombol Konfirmasi Pinjam untuk masuk ke Form Pengajuan
        binding.btnKonfirmasiPinjam.setOnClickListener {
            val total = keranjangAdapter.hitungTotalTerpilih()
            if (total > 0) {
                // Lompat masuk ke Halaman Form Pengajuan Pinjam
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, FormPinjamFragment()) // PERBAIKAN: Ubah menjadi fragmentContainer
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(context, "Pilih minimal 1 buku untuk dipinjam!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi memperbarui komponen teks jumlah item terpilih secara berkala
    private fun updateRingkasanTeks(total: Int) {
        binding.tvTotalItemHeader.text = "$total buku terpilih"
        binding.tvTotalItemBawah.text = "$total Buku"
    }
}