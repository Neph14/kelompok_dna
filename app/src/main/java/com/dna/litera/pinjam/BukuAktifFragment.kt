package com.dna.litera.pinjam

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dna.litera.R
import com.dna.litera.buku.BukuSayaAdapter
import com.dna.litera.data.BukuMilikSaya
import com.dna.litera.databinding.FragmentBukuAktifBinding
import com.google.firebase.firestore.FirebaseFirestore

class BukuAktifFragment : Fragment(R.layout.fragment_buku_aktif) {

    private var _binding: FragmentBukuAktifBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BukuSayaAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBukuAktifBinding.bind(view)

        setupRecyclerView()
        muatDataDariFirestore()
    }

    private fun setupRecyclerView() {
        adapter = BukuSayaAdapter(emptyList(), isHistory = false) { buku ->
            Toast.makeText(requireContext(), "Permintaan perpanjang '${buku.judul}' diajukan!", Toast.LENGTH_SHORT).show()
        }
        binding.rvBukuAktif.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBukuAktif.adapter = adapter
    }

    private fun muatDataDariFirestore() {
        val npmUser = arguments?.getString("ARG_NPM") ?: "24111019"

        db.collection("peminjaman")
            .whereEqualTo("npm", npmUser)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val listBukuAktif = mutableListOf<BukuMilikSaya>()
                value?.documents?.forEach { doc ->
                    val status = doc.getString("status_peminjaman") ?: ""
                    
                    if (status in listOf("DIPINJAM", "MENUNGGU_KONFIRMASI", "AKTIF", "PENDING", "ACTIVE")) {
                        val tglKembali = doc.getLong("tanggal_kembali") ?: doc.getLong("tanggalKembali") ?: 0L
                        val bukuListRaw = doc.get("buku_dipinjam") as? List<Map<String, Any>> ?: doc.get("daftarBuku") as? List<Map<String, Any>>
                        
                        bukuListRaw?.forEach { map ->
                            val url = map["imageUrl"] as? String ?: map["image_url"] as? String ?: ""
                            
                            listBukuAktif.add(BukuMilikSaya(
                                idBuku = (map["id_buku"] ?: map["idBuku"] ?: "").toString(),
                                judul = (map["judul"] ?: "Tanpa Judul").toString(),
                                penulis = (map["penulis"] ?: "Anonim").toString(),
                                imageUrl = url,
                                statusPeminjaman = status, // Pass status dokumen ke item buku
                                tanggalKembali = tglKembali
                            ))
                        }
                    }
                }

                if (listBukuAktif.isEmpty()) {
                    binding.rvBukuAktif.visibility = View.GONE
                    binding.tvEmptyBukuAktif.visibility = View.VISIBLE
                } else {
                    binding.rvBukuAktif.visibility = View.VISIBLE
                    binding.tvEmptyBukuAktif.visibility = View.GONE
                    adapter.updateData(listBukuAktif)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}