package com.example.litera.buku

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.litera.R
import com.example.litera.data.Buku
import com.example.litera.databinding.FragmentDetailBukuBinding
import com.example.litera.home.BukuHorizontalAdapter

class DetailBukuFragment : Fragment(R.layout.fragment_detail_buku) {

    private lateinit var binding: FragmentDetailBukuBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailBukuBinding.bind(view)

        // Ambil data buku (Misal dari bundle/arguments)
        val judul = arguments?.getString("judul") ?: "Harry Potter"
        val penulis = arguments?.getString("penulis") ?: "J. K. Rowling"
        val coverUrl = arguments?.getString("coverUrl") ?: ""

        // Tampilkan Teks
        binding.tvJudulDetail.text = judul
        binding.tvPenulisDetail.text = penulis

        // 1. Loading Gambar & Ekstrak Warna Background
        loadBookCoverAndPalette(coverUrl)

        // 2. Setup RecyclerView Buku Sejenis
        setupBukuSejenis()

        // 3. Tombol Kembali
        binding.btnBackDetail.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 4. Tombol Tambah ke Daftar Pinjam
        binding.btnTambahDaftarPinjam.setOnClickListener {
            Toast.makeText(context, "$judul ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBookCoverAndPalette(url: String) {
        // Gunakan Glide untuk mengambil Bitmap
        Glide.with(this)
            .asBitmap()
            .load(url)
            .placeholder(R.drawable.placeholder_buku)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Masukkan ke ImageView
                    binding.ivCoverDetail.setImageBitmap(resource)

                    // Gunakan Palette untuk ambil warna
                    Palette.from(resource).generate { palette ->
                        // Ambil warna Vibrant atau Muted, jika gagal pakai abu-abu
                        val color = palette?.getLightVibrantColor(
                            ContextCompat.getColor(requireContext(), R.color.white)
                        ) ?: ContextCompat.getColor(requireContext(), R.color.white)

                        // Terapkan Gradient ke background
                        updateBackgroundColor(color)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun updateBackgroundColor(color: Int) {
        // Membuat gradien dari warna cover ke putih agar terlihat halus
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(color, Color.WHITE)
        )
        binding.viewBackgroundColor.background = gradient
    }

    private fun setupBukuSejenis() {
        val listSejenis = mutableListOf<Buku>()
        // Data dummy untuk UI
        listSejenis.add(Buku("1", "The Hobbit", "J.R.R. Tolkien"))
        listSejenis.add(Buku("2", "Fantastic Beasts", "Newt Scamander"))
        listSejenis.add(Buku("3", "Percy Jackson", "Rick Riordan"))

        binding.rvBukuSejenis.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBukuSejenis.adapter = BukuHorizontalAdapter(listSejenis)
    }
}