package com.example.litera.pinjam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.litera.data.KeranjangItem
import com.example.litera.databinding.FragmentDaftarPinjamBinding // Sesuaikan dengan nama binding layout Anda

class KeranjangFragment : Fragment() {

    private var _binding: FragmentDaftarPinjamBinding? = null
    private val binding get() = _binding!!

    private lateinit var keranjangAdapter: KeranjangAdapter
    private var dataKeranjang = ArrayList<KeranjangItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDaftarPinjamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data dummy sementara (Nanti bagian ini yang kita hubungkan ke DB)
        loadDataKeranjangSementaran()

        setupRecyclerView()
        hitungTotalBuku()

        // Aksi Tombol Konfirmasi Pinjam / Checkout
        binding.btnKonfirmasiPinjam.setOnClickListener {
            Toast.makeText(context, "Memproses peminjaman ${dataKeranjang.size} judul buku...", Toast.LENGTH_SHORT).show()
            // Bagian ini nanti akan menembak Firestore untuk membuat dokumen transaksi baru
        }
    }

    private fun setupRecyclerView() {
        keranjangAdapter = KeranjangAdapter(dataKeranjang,
            onJumlahChanged = {
                hitungTotalBuku()
            },
            onHapusItem = { posisi ->
                dataKeranjang.removeAt(posisi)
                keranjangAdapter.notifyItemRemoved(posisi)
                hitungTotalBuku()
            }
        )

        binding.rvKeranjangPinjam.apply { // Sesuaikan ID RecyclerView di XML Keranjang Anda
            layoutManager = LinearLayoutManager(context)
            adapter = keranjangAdapter
        }
    }

    private fun hitungTotalBuku() {
        var total = 0
        for (item in dataKeranjang) {
            total += item.jumlahPinjam
        }
        // Pasang total item ke boks teks ringkasan biaya/peminjaman figma Anda
        binding.tvTotalItemBawah.text = "$total Buku"
    }

    private fun loadDataKeranjangSementaran() {
        // Data tiruan lokal sebelum nanti kita buat fitur add to cart dari database
        dataKeranjang.clear()
        dataKeranjang.add(
            KeranjangItem(
                "BK_001",
                "Statistika dan Probabilitas",
                "Dr. Boediono",
                ""
            )
        )
        dataKeranjang.add(KeranjangItem("BK_003", "Pulang", "Tere Liye", ""))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}