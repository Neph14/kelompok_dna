package com.dna.litera.profile

import com.dna.litera.R

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dna.litera.auth.LoginActivity
import com.dna.litera.databinding.FragmentProfilBinding
import com.google.firebase.firestore.FirebaseFirestore

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Ambil NPM yang dialirkan dari MainActivity atau SharedPref Sesi
        val sharedPref = requireActivity().getSharedPreferences("SesiLitera", Context.MODE_PRIVATE)
        val npmUser = arguments?.getString("ARG_NPM") ?: sharedPref.getString("LOGIN_NPM", "") ?: ""

        if (npmUser.isNotEmpty()) {
            binding.shimmerProfile.startShimmer()
            binding.shimmerProfile.visibility = View.VISIBLE
            binding.scrollProfileContent.visibility = View.GONE

            // Set nilai cadangan awal sembari menunggu data dari Firestore selesai dimuat
            binding.tvEmailProfil.text = "$npmUser@widyatama.ac.id"

            // 2. Ambil data profil lengkap dari Firestore secara realtime / sekali panggil
            db.collection("users").document(npmUser)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Mengisi nama asli dari database
                        binding.tvNamaProfil.text = document.getString("nama") ?: "Nevan Nurrahman"

                        // AMBIL EMAIL ASLI DARI DATABASE FIRESTORE (Kunci Perbaikan!)
                        val emailDariDatabase = document.getString("email")
                        if (!emailDariDatabase.isNullOrEmpty()) {
                            binding.tvEmailProfil.text = emailDariDatabase
                        }

                        binding.shimmerProfile.stopShimmer()
                        binding.shimmerProfile.visibility = View.GONE
                        binding.scrollProfileContent.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfilFragment", "Gagal load data profil dari Firestore: ${e.message}")
                    binding.tvNamaProfil.text = "Nevan Nurrahman Adi Putra"
                    
                    binding.shimmerProfile.stopShimmer()
                    binding.shimmerProfile.visibility = View.GONE
                    binding.scrollProfileContent.visibility = View.VISIBLE
                }
        }

        // --- 3. LOGIKA TOMBOL LOGOUT ---
        binding.btnLogout.setOnClickListener {
            val editor = sharedPref.edit()
            editor.clear() // Bersihkan sesi login
            editor.apply()

            val intent = Intent(activity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }

        // --- 4. LOGIKA TOMBOL EDIT PROFIL ---
        binding.cvEditProfil.setOnClickListener {
            val fragmentEditProfil = EditProfilFragment()
            
            // Kirim NPM ke EditProfilFragment
            val bundle = Bundle()
            bundle.putString("ARG_NPM", npmUser)
            fragmentEditProfil.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragmentEditProfil)
                .addToBackStack(null)
                .commit()
        }

        // --- 5. LOGIKA TOMBOL TERMS OF SERVICE ---

        // --- 6. LOGIKA TOMBOL TERMS OF SERVICE ---
        binding.btnTermsOfService.setOnClickListener {
            val fragmentToS = TermsOfServiceFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragmentToS)
                .addToBackStack(null)
                .commit()
        }

        // --- 7. LOGIKA TOMBOL CHAT WITH US (REDIRECT TO WEBSITE WIDYATAMA) ---
        binding.btnChatWithUs.setOnClickListener {
            val urlKampus = "https://youtu.be/eioYulMp_5k?si=GjTWljzGSBQWX5MM&t=3" // :)
            val intentBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(urlKampus))

            try {
                startActivity(intentBrowser)
            } catch (e: Exception) {
                Toast.makeText(context, "Tidak ada browser yang mendukung", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}