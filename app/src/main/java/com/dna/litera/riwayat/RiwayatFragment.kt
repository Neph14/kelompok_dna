package com.dna.litera.riwayat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dna.litera.data.RiwayatItem
import com.dna.litera.databinding.FragmentRiwayatBinding // Sesuaikan nama binding layout Fragment Anda
import com.google.firebase.firestore.FirebaseFirestore

class RiwayatFragment : Fragment() {

    private var _binding: FragmentRiwayatBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var riwayatAdapter: RiwayatAdapter
    private val listRiwayat = ArrayList<RiwayatItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Ambil session NPM mahasiswa yang sedang aktif login
        val sharedPref = activity?.getSharedPreferences("LiteraPref", Context.MODE_PRIVATE)
        val npmLogin = sharedPref?.getString("SESSION_NPM", "") ?: ""

        if (npmLogin.isNotEmpty()) {
            ambilDataRiwayatDariFirestore(npmLogin)
        }
    }

    private fun setupRecyclerView() {
        riwayatAdapter = RiwayatAdapter(listRiwayat)
        binding.rvRiwayat.apply { // Sesuaikan ID RecyclerView di XML Fragment Riwayat Anda
            layoutManager = LinearLayoutManager(context)
            adapter = riwayatAdapter
            setHasFixedSize(true)
        }
    }

    private fun ambilDataRiwayatDariFirestore(npm: String) {
        // Melakukan query mencari data di koleksi "transaksi" yang kolom NPM-nya cocok
        db.collection("transaksi")
            .whereEqualTo("npm", npm)
            .get()
            .addOnSuccessListener { result ->
                listRiwayat.clear()
                for (document in result) {
                    val riwayat = document.toObject(RiwayatItem::class.java)
                    listRiwayat.add(riwayat)
                }
                riwayatAdapter.updateData(listRiwayat)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Gagal memuat riwayat: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}