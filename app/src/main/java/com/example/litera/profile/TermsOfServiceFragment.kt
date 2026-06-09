package com.example.litera.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.litera.auth.LoginActivity
import com.example.litera.databinding.FragmentTermsOfServiceBinding

class TermsOfServiceFragment : Fragment() {

    private var _binding: FragmentTermsOfServiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsOfServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. LOGIKA UTAMA: DECLINE & CLOSE (SINKRON ID XML) ---
        // --- 1. LOGIKA UTAMA: DECLINE, CLEAR SESSION, & TUTUP APLIKASI TOTAL ---
        binding.btnDeclineTos.setOnClickListener {
            // Langkah A: Hapus paksa session login dari SharedPreferences
            val sharedPref = requireActivity().getSharedPreferences("SesiLitera", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()

            // Langkah B: Tutup aplikasi secara total seketika (Exit Application)
            requireActivity().finishAffinity()
        }

        // --- 2. LOGIKA: ACCEPT AND CONTINUE (SINKRON ID XML) ---
        binding.btnAcceptTos.setOnClickListener {
            // Karena pengguna setuju, kembalikan secara aman ke halaman ProfilFragment
            parentFragmentManager.popBackStack()
        }

        // --- 3. LOGIKA TOMBOL BACK PANAH ORANYE ---
        binding.btnBackTos.setOnClickListener {
            // Kembali ke halaman sebelumnya tanpa menghapus session login
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}