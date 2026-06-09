package com.example.litera.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.litera.MainActivity
import com.example.litera.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val db = FirebaseFirestore.getInstance()

    // Variabel penanda status password (false artinya tersembunyi)
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // =============================================================
        // 1. CEK SESI SECARA INSTAN SEBELUM MEMBUKA LAYOUT LOGIN
        // =============================================================
        val sharedPref = getSharedPreferences("SesiLitera", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

        if (isLoggedIn) {
            // Jika sesi tersimpan sejati, langsung bypass ke MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Tutup LoginActivity agar tidak bisa di-back kembali ke sini
            return   // Hentikan sisa kode onCreate di bawahnya agar tidak memakan memori
        }
        // =============================================================

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogIn.setOnClickListener {
            aksiloginPureNPM()
        }

        // --- LOGIKA TOMBOL MATA UNTUK INTIP PASSWORD ---
        binding.ivEyeToggle.setOnClickListener {
            if (isPasswordVisible) {
                // Sembunyikan password -> Ubah jadi titik-titik
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivEyeToggle.setColorFilter(android.graphics.Color.parseColor("#A0A0A0"))
                isPasswordVisible = false
            } else {
                // Tampilkan password -> Ubah jadi teks biasa
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivEyeToggle.setColorFilter(android.graphics.Color.parseColor("#F37321"))
                isPasswordVisible = true
            }

            // Pindahkan kursor ketikan ke ujung paling kanan teks agar tidak reset ke awal
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
    }

    private fun aksiloginPureNPM() {
        val npm = binding.etNPM.text.toString().trim()
        val passwordInput = binding.etPassword.text.toString().trim()

        if (npm.isEmpty() || passwordInput.isEmpty()) {
            Toast.makeText(this, "NPM dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(npm)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val passwordDatabase = document.getString("password") ?: ""

                    if (passwordInput == passwordDatabase) {
                        Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                        // SIMPAN SESI LOGIN (Supaya tidak perlu login ulang saat aplikasi ditutup)
                        val sharedPref = getSharedPreferences("SesiLitera", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("LOGIN_NPM", npm)
                        editor.putBoolean("IS_LOGGED_IN", true)
                        editor.apply()

                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("EXTRA_NPM", npm)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Login Gagal: Password salah!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Login Gagal: NPM tidak terdaftar!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Kendala koneksi: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}