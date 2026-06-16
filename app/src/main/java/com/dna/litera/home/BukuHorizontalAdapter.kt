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
import com.dna.litera.databinding.ItemBukuHorizontalBinding

class BukuHorizontalAdapter(
    private var listBuku: List<Buku>,
    private val onBukuClicked: (Buku) -> Unit = {}
) : RecyclerView.Adapter<BukuHorizontalAdapter.ViewHolder>() {

    fun updateData(newList: List<Buku>) {
        listBuku = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBukuHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listBuku[position])
    }

    override fun getItemCount(): Int = listBuku.size

    inner class ViewHolder(private val binding: ItemBukuHorizontalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(buku: Buku) {
            binding.tvJudulHorizontal.text = buku.judul
            binding.tvPenulisHorizontal.text = buku.penulis

            itemView.setOnClickListener { onBukuClicked(buku) }

            // Load Gambar menggunakan properti imageUrl terbaru
            Glide.with(itemView.context)
                .asBitmap()
                .load(buku.imageUrl) // UBAH DI SINI
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        binding.ivCoverHorizontal.setImageBitmap(resource)

                        Palette.from(resource).generate { palette ->
                            val warnaGelapDominan = palette?.getDarkMutedColor(Color.parseColor("#66000000"))
                                ?: Color.parseColor("#66000000")

                            val warnaAcrylicTransparan = (warnaGelapDominan and 0x00FFFFFF) or 0xBB000000.toInt()
                            (binding.root.getChildAt(0) as ViewGroup).getChildAt(1).setBackgroundColor(warnaAcrylicTransparan)
                        }
                    }
                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                })
        }
    }
}