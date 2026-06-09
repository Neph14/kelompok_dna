package com.example.litera.pinjam

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.litera.R
import com.example.litera.buku.BukuSayaAdapter
import com.example.litera.data.BukuMilikSaya
import com.example.litera.databinding.FragmentHistoryBinding
import com.google.firebase.firestore.FirebaseFirestore

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BukuSayaAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        setupRecyclerView()
        muatDataHistoryDariFirestore()
    }

    private fun setupRecyclerView() {
        adapter = BukuSayaAdapter(emptyList(), isHistory = true) { buku ->
            Toast.makeText(requireContext(), "Aksi history: ${buku.judul}", Toast.LENGTH_SHORT).show()
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun muatDataHistoryDariFirestore() {
        val npmUser = arguments?.getString("ARG_NPM") ?: "24111019"

        db.collection("peminjaman")
            .whereEqualTo("npm", npmUser)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val listHistory = mutableListOf<BukuMilikSaya>()
                value?.documents?.forEach { doc ->
                    val status = doc.getString("status_peminjaman") ?: ""
                    
                    if (status in listOf("SELESAI", "RETURNED", "KEMBALI", "FINISHED")) {
                        val bukuListRaw = doc.get("buku_dipinjam") as? List<Map<String, Any>> ?: doc.get("daftarBuku") as? List<Map<String, Any>>
                        
                        bukuListRaw?.forEach { map ->
                            val url = map["imageUrl"] as? String ?: map["image_url"] as? String ?: ""

                            listHistory.add(BukuMilikSaya(
                                idBuku = (map["id_buku"] ?: map["idBuku"] ?: "").toString(),
                                judul = (map["judul"] ?: "Tanpa Judul").toString(),
                                penulis = (map["penulis"] ?: "Anonim").toString(),
                                imageUrl = url,
                                tanggalKembali = 0L
                            ))
                        }
                    }
                }

                if (listHistory.isEmpty()) {
                    binding.rvHistory.visibility = View.GONE
                    binding.tvEmptyHistory.visibility = View.VISIBLE
                } else {
                    binding.rvHistory.visibility = View.VISIBLE
                    binding.tvEmptyHistory.visibility = View.GONE
                    adapter.updateData(listHistory)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}