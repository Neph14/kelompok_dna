package com.dna.litera.pinjam

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dna.litera.R
import com.dna.litera.databinding.FragmentBukuSayaBinding

class BukuSayaFragment : Fragment(R.layout.fragment_buku_saya) {

    private lateinit var binding: FragmentBukuSayaBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBukuSayaBinding.bind(view)

        checkCatatanStatus()
        setupBukuSayaRecycler()

        // Tombol Mengerti di klik -> Simpan status agar tidak muncul lagi, lalu tampilkan list
        binding.btnMengertiCatatan.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences("LiteraPref", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("isFirstTimeCatatan", false).apply()

            binding.layoutCatatanOverlay.visibility = View.GONE
            binding.rvBukuSaya.visibility = View.VISIBLE
        }

        // Tombol Ikon Informasi -> Paksa tampilkan kembali catatan aturan
        binding.btnInfoCatatan.setOnClickListener {
            binding.layoutCatatanOverlay.visibility = View.VISIBLE
            binding.rvBukuSaya.visibility = View.GONE
        }
    }

    private fun checkCatatanStatus() {
        val sharedPref = requireActivity().getSharedPreferences("LiteraPref", Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTimeCatatan", true)

        if (isFirstTime) {
            binding.layoutCatatanOverlay.visibility = View.VISIBLE
            binding.rvBukuSaya.visibility = View.GONE
        } else {
            binding.layoutCatatanOverlay.visibility = View.GONE
            binding.rvBukuSaya.visibility = View.VISIBLE
        }
    }

    private fun setupBukuSayaRecycler() {
        // Bagian ini nantinya akan dipasang Adapter Khusus Buku Saya yang mengelola sisa waktu pinjam
        binding.rvBukuSaya.layoutManager = LinearLayoutManager(context)
    }
}