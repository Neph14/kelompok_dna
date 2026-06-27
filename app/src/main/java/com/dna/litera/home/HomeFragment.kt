package com.dna.litera.home

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dna.litera.R
import com.dna.litera.buku.DetailBukuActivity
import com.dna.litera.data.Buku
import com.dna.litera.data.local.AppDatabase
import com.dna.litera.data.local.BukuCart
import com.dna.litera.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    private lateinit var adapterJurusan: BukuHorizontalAdapter
    private lateinit var adapterRekomendasi: BukuVertikalAdapter

    private val listSemuaBuku = ArrayList<Buku>()
    private val listBukuJurusan = ArrayList<Buku>()

    private var jurusanUser: String = ""
    private var currentKategori = "Semua"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupFilterCategoryButtons()
        ambilDataProfilDanJurusanMahasiswa()
        ambilDataBukuDariFirestore()

        binding.btnKeranjangPinjam.setOnClickListener {
            val intent = Intent(requireContext(), com.dna.litera.pinjam.CartActivity::class.java)
            startActivity(intent)
        }

        binding.etSearchHome.setOnClickListener {
            // Pindah ke tab Search di Bottom Navigation
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
            bottomNav.selectedItemId = R.id.nav_search
        }
    }

    private fun setupRecyclerViews() {
        val onBukuClicked: (Buku) -> Unit = { bukuTerpilih ->
            val intent = Intent(activity, DetailBukuActivity::class.java).apply {
                putExtra("EXTRA_BUKU", bukuTerpilih)
            }
            startActivity(intent)
        }

        // AKSI TAMBAH KE KERANJANG DARI TOMBOL PLUS
        val onPlusClicked: (Buku) -> Unit = { buku ->
            tambahBukuKeKeranjang(buku)
        }

        adapterJurusan = BukuHorizontalAdapter(listBukuJurusan, onBukuClicked)
        binding.rvBukuSistemInformasi.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = adapterJurusan
            setHasFixedSize(true)
        }

        adapterRekomendasi = BukuVertikalAdapter(listSemuaBuku, onBukuClicked, onPlusClicked)
        binding.rvRekomendasiHome.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterRekomendasi
            setHasFixedSize(true)
        }
    }

    private fun tambahBukuKeKeranjang(buku: Buku) {
        // CEK STATUS KETERSEDIAAN
        if (!buku.status.equals("Tersedia", ignoreCase = true)) {
            Toast.makeText(requireContext(), "Buku ini lagi kosong, coba lain waktu", Toast.LENGTH_SHORT).show()
            return
        }

        val database = AppDatabase.getDatabase(requireContext())
        val cartDao = database.bukuCartDao()

        val itemBaru = BukuCart(
            id_buku = buku.judul ?: "", 
            judul = buku.judul ?: "Tanpa Judul",
            penulis = buku.penulis ?: "Anonim",
            imageUrl = buku.imageUrl ?: ""
        )

        lifecycleScope.launch(Dispatchers.IO) {
            cartDao.tambahKeKeranjang(itemBaru)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "'${buku.judul}' berhasil ditambah ke keranjang!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFilterCategoryButtons() {
        binding.btnKategoriSemua.setOnClickListener {
            currentKategori = "Semua"
            aturVisualTombolAktif(binding.btnKategoriSemua)
            filterDanUrutkanBukuRekomendasi()
        }

        binding.btnKategoriModul.setOnClickListener {
            currentKategori = "Modul"
            aturVisualTombolAktif(binding.btnKategoriModul)
            filterDanUrutkanBukuRekomendasi()
        }

        binding.btnKategoriNovel.setOnClickListener {
            currentKategori = "Novel"
            aturVisualTombolAktif(binding.btnKategoriNovel)
            filterDanUrutkanBukuRekomendasi()
        }

        binding.btnKategoriKomik.setOnClickListener {
            currentKategori = "Komik"
            aturVisualTombolAktif(binding.btnKategoriKomik)
            filterDanUrutkanBukuRekomendasi()
        }
    }

    private fun aturVisualTombolAktif(tombolTerpilih: Button) {
        val listTombol = listOf(
            binding.btnKategoriSemua,
            binding.btnKategoriModul,
            binding.btnKategoriNovel,
            binding.btnKategoriKomik
        )

        for (button in listTombol) {
            if (button == tombolTerpilih) {
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F37321"))
                button.setTextColor(Color.WHITE)
            } else {
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EAEAEA"))
                button.setTextColor(Color.parseColor("#555555"))
            }
        }
    }

    private fun filterDanUrutkanBukuRekomendasi() {
        var listHasilSaringan = listSemuaBuku.toList()

        if (currentKategori != "Semua") {
            listHasilSaringan = listHasilSaringan.filter { buku ->
                buku.tipe.equals(currentKategori, ignoreCase = true)
            }
        }

        listHasilSaringan = listHasilSaringan.sortedWith(
            compareByDescending<Buku> {
                it.status.equals("Tersedia", ignoreCase = true)
            }.thenBy {
                it.judul
            }
        )

        if (listHasilSaringan.isEmpty()) {
            binding.rvRekomendasiHome.visibility = View.GONE
            binding.tvDataKosongHome.visibility = View.VISIBLE
        } else {
            binding.rvRekomendasiHome.visibility = View.VISIBLE
            binding.tvDataKosongHome.visibility = View.GONE
        }

        adapterRekomendasi.updateData(listHasilSaringan)
    }

    private fun ambilDataProfilDanJurusanMahasiswa() {
        val npmUser = arguments?.getString("ARG_NPM") ?: ""

        if (npmUser.isNotEmpty()) {
            db.collection("users").document(npmUser)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nama = document.getString("nama") ?: "Nevan Nurrahman"
                        jurusanUser = document.getString("jurusan") ?: "Sistem Informasi"

                        binding.tvSalamMahasiswa.text = "Halo, $nama"

                        if (jurusanUser.isNotEmpty()) {
                            binding.tvJudulKategori.text = jurusanUser
                            saringBukuBerdasarkanJurusan()
                        }
                    } else {
                        binding.tvSalamMahasiswa.text = "Halo, Mahasiswa"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeFragment", "Gagal ambil profil: ${exception.message}")
                    binding.tvSalamMahasiswa.text = "Halo, Mahasiswa"
                }
        } else {
            binding.tvSalamMahasiswa.text = "Halo, Mahasiswa"
        }
    }

    private fun ambilDataBukuDariFirestore() {
        binding.shimmerHome.startShimmer()
        binding.shimmerHome.visibility = View.VISIBLE
        binding.scrollHomeContent.visibility = View.GONE

        db.collection("koleksi_buku")
            .get()
            .addOnSuccessListener { result ->
                listSemuaBuku.clear()

                for (document in result) {
                    val buku = document.toObject(Buku::class.java)
                    listSemuaBuku.add(buku)
                }

                filterDanUrutkanBukuRekomendasi()
                saringBukuBerdasarkanJurusan()

                binding.shimmerHome.stopShimmer()
                binding.shimmerHome.visibility = View.GONE
                binding.scrollHomeContent.visibility = View.VISIBLE
            }
            .addOnFailureListener { exception ->
                binding.shimmerHome.stopShimmer()
                binding.shimmerHome.visibility = View.GONE
                binding.scrollHomeContent.visibility = View.VISIBLE

                if (isAdded) {
                    Toast.makeText(context, "Gagal memuat buku: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saringBukuBerdasarkanJurusan() {
        listBukuJurusan.clear()

        val namaJurusanSaringan = if (jurusanUser.isNotEmpty()) jurusanUser else "Sistem Informasi"

        for (buku in listSemuaBuku) {
            val jurusanBuku = buku.jurusan ?: ""
            val statusBuku = buku.status ?: ""

            if (jurusanBuku.equals(namaJurusanSaringan, ignoreCase = true) &&
                statusBuku.equals("Tersedia", ignoreCase = true)) {

                listBukuJurusan.add(buku)
            }
        }

        adapterJurusan.updateData(listBukuJurusan)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}