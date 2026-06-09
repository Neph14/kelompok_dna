package com.example.litera.pinjam

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.litera.R
import com.example.litera.databinding.FragmentBukuSayaParentBinding
import com.google.android.material.tabs.TabLayout

class BukuSayaParentFragment : Fragment(R.layout.fragment_buku_saya_parent) {

    private lateinit var binding: FragmentBukuSayaParentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBukuSayaParentBinding.bind(view)

        // Tombol Kembali
        binding.btnBackBukuSaya.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Tampilan default saat pertama kali dibuka: Menampilkan daftar buku aktif
        pindahSubFragment(BukuAktifFragment())

        // Logika mendengarkan perpindahan klik pada Tab
        binding.tabBukuSaya.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> pindahSubFragment(BukuAktifFragment()) // Tab 1: Buku Aktif
                    1 -> pindahSubFragment(HistoryFragment()) // Tab 2: History
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun pindahSubFragment(fragment: Fragment) {
        // Teruskan data NPM dari Parent ke Child Fragment
        fragment.arguments = this.arguments

        childFragmentManager.beginTransaction()
            .replace(R.id.containerChildBuku, fragment)
            .commit()
    }
}