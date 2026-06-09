package com.example.litera

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.litera.home.HomeFragment
import com.example.litera.buku.SearchFragment
import com.example.litera.profile.ProfilFragment // Pastikan folder/package profil sudah dibuat
import com.example.litera.pinjam.BukuSayaParentFragment
import com.example.litera.auth.LoginActivity
import com.example.litera.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var npmUser: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 1. CEK SESI LOGIN TERLEBIH DAHULU ---
        val sharedPref = getSharedPreferences("SesiLitera", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)

        if (!isLoggedIn) {
            // Jika belum login, tendang kembali ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Jika sudah login, ambil NPM dari Sesi SharedPreferences atau Intent cadangan
        npmUser = sharedPref.getString("LOGIN_NPM", null) ?: intent.getStringExtra("EXTRA_NPM")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Halaman Pertama Kali Terbuka (Default ke HomeFragment)
        if (savedInstanceState == null) {
            muatFragment(HomeFragment())
        }

        // Logika Klik Menu Navigasi Bawah
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    muatFragment(HomeFragment())
                    true
                }
                R.id.nav_search -> {
                    muatFragment(SearchFragment())
                    true
                }
                R.id.nav_buku_saya -> {
                    muatFragment(BukuSayaParentFragment())
                    true
                }
                R.id.nav_profil -> {
                    // 2. HUBUNGKAN KE PROFIL FRAGMENT SEKARANG
                    muatFragment(ProfilFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Menghandle transaksi fragment + kirim data NPM otomatis ke semua fragment
    private fun muatFragment(fragment: Fragment) {
        npmUser?.let { npm ->
            val bundle = Bundle().apply {
                putString("ARG_NPM", npm)
            }
            fragment.arguments = bundle
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}