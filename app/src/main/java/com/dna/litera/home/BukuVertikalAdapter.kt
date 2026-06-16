package com.dna.litera.home

import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.dna.litera.data.Buku
import com.dna.litera.databinding.ItemBukuVertikalBinding

class BukuVertikalAdapter(
    private var listBuku: List<Buku>,
    private val onBukuClicked: (Buku) -> Unit = {},
    private val onPlusClicked: (Buku) -> Unit = {} // Callback untuk tombol tambah
) : RecyclerView.Adapter<BukuVertikalAdapter.ViewHolder>() {

    fun updateData(newList: List<Buku>) {
        listBuku = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBukuVertikalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listBuku[position])
    }

    override fun getItemCount(): Int = listBuku.size

    inner class ViewHolder(private val binding: ItemBukuVertikalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(buku: Buku) {
            binding.tvJudulBuku.text = buku.judul
            binding.tvPenulisBuku.text = buku.penulis
            binding.tvPenulisBuku.setTextColor(Color.parseColor("#444444"))

            val statusBuku = buku.status ?: "Tidak Tersedia"
            binding.tvStatusBuku.text = statusBuku

            if (statusBuku.equals("Tersedia", ignoreCase = true)) {
                binding.tvStatusBuku.setTextColor(Color.parseColor("#1B5E20"))
            } else {
                binding.tvStatusBuku.setTextColor(Color.parseColor("#B71C1C"))
            }

            itemView.setOnClickListener { onBukuClicked(buku) }

            // Tombol Tambah Cepat (Plus)
            binding.btnTambahCepat.setOnClickListener {
                onPlusClicked(buku)
            }

            Glide.with(itemView.context)
                .asBitmap()
                .load(buku.imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        binding.ivCoverBuku.setImageBitmap(resource)

                        Palette.from(resource).generate { palette ->
                            val warnaAcrylicTerang = palette?.getLightMutedColor(Color.parseColor("#F5F5F5"))
                                ?: Color.parseColor("#F5F5F5")

                            binding.root.setCardBackgroundColor(warnaAcrylicTerang)
                        }
                    }
                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                })
        }
    }
}