package com.dna.litera.pinjam

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dna.litera.databinding.ActivityFormPinjamBinding
import com.google.firebase.firestore.FirebaseFirestore

class FormPinjamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormPinjamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormPinjamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // Muat data user otomatis
        muatDataUserOtomatis()

        // 1. Tombol Close (X) di Pojok Kanan Atas Toolbar
        binding.btnCloseForm.setOnClickListener {
            finish() 
        }

        // 2. Tombol Simpan Form Data Diri Peminjam
        binding.btnSimpanFormData.setOnClickListener {
            val nama = binding.etNamaLengkapForm.text.toString().trim()
            val npm = binding.etNpmForm.text.toString().trim()
            val prodi = binding.etProdiForm.text.toString().trim()

            if (nama.isEmpty() || npm.isEmpty() || prodi.isEmpty()) {
                Toast.makeText(this, "Harap lengkapi semua data diri Anda!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Data peminjam berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun muatDataUserOtomatis() {
        val sharedPref = getSharedPreferences("SesiLitera", MODE_PRIVATE)
        val npmUser = sharedPref.getString("LOGIN_NPM", "24111019") ?: "24111019"

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(npmUser).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nama = document.getString("name") ?: document.getString("nama") ?: ""
                    val prodi = document.getString("jurusan") ?: ""
                    
                    binding.etNamaLengkapForm.setText(nama)
                    binding.etNpmForm.setText(npmUser)
                    binding.etProdiForm.setText(prodi)
                }
            }
    }
}