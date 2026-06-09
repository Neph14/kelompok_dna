package com.example.litera.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.litera.R
import com.example.litera.databinding.FragmentEditProfilBinding
import com.google.firebase.firestore.FirebaseFirestore

class EditProfilFragment : Fragment(R.layout.fragment_edit_profil) {

    private lateinit var binding: FragmentEditProfilBinding
    private val db = FirebaseFirestore.getInstance()
    private var npmUser: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditProfilBinding.bind(view)

        // Ambil NPM dari arguments atau SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("SesiLitera", Context.MODE_PRIVATE)
        npmUser = arguments?.getString("ARG_NPM") ?: sharedPref.getString("LOGIN_NPM", "") ?: ""

        // Kunci input email agar tidak bisa diubah (sesuai permintaan: hanya nama yang bisa dirubah)
        binding.etEditEmail.isEnabled = false
        binding.etEditEmail.alpha = 0.6f

        if (npmUser.isNotEmpty()) {
            loadUserData()
        }

        binding.btnBackEditProfil.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnAmbilFoto.setOnClickListener {
            Toast.makeText(context, "Fitur ganti foto belum tersedia", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveChanges.setOnClickListener {
            val namaBaru = binding.etEditNama.text.toString().trim()

            if (namaBaru.isEmpty()) {
                Toast.makeText(context, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            } else {
                updateProfile(namaBaru)
            }
        }
    }

    private fun loadUserData() {
        db.collection("users").document(npmUser).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    binding.etEditNama.setText(document.getString("nama"))
                    binding.etEditEmail.setText(document.getString("email"))
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditProfilFragment", "Gagal load data: ${e.message}")
            }
    }

    private fun updateProfile(namaBaru: String) {
        if (npmUser.isEmpty()) return

        val dataUpdate = mapOf(
            "nama" to namaBaru
        )

        db.collection("users").document(npmUser)
            .update(dataUpdate)
            .addOnSuccessListener {
                Toast.makeText(context, "Nama berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal memperbarui: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}