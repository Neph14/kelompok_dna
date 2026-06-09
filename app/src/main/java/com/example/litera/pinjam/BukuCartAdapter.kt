package com.example.litera.pinjam

import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.litera.R
import com.example.litera.data.local.BukuCart
import com.example.litera.databinding.ItemCartBinding

class BukuCartAdapter(
    private var listBuku: List<BukuCart>,
    private val onHapusClick: (BukuCart) -> Unit
) : RecyclerView.Adapter<BukuCartAdapter.ViewHolder>() {

    fun updateData(newList: List<BukuCart>) {
        listBuku = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val buku = listBuku[position]
        holder.binding.apply {
            tvJudulCart.text = buku.judul
            tvPenulisCart.text = buku.penulis

            // Reset ke warna default pastel terang (abu-abu pudar)
            root.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
            ivCoverCart.setImageResource(R.drawable.placeholder_buku)

            if (!buku.imageUrl.isNullOrEmpty()) {
                Glide.with(root.context)
                    .asBitmap()
                    .load(buku.imageUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            ivCoverCart.setImageBitmap(resource)

                            // Ekstraksi warna pastel (LightMuted) seperti di Home & Buku Saya
                            Palette.from(resource).generate { palette ->
                                val warnaAcrylicTerang = palette?.getLightMutedColor(Color.parseColor("#F5F5F5"))
                                    ?: Color.parseColor("#F5F5F5")
                                
                                root.setCardBackgroundColor(warnaAcrylicTerang)
                            }
                        }
                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            ivCoverCart.setImageResource(R.drawable.placeholder_buku)
                        }
                        override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            root.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
                            ivCoverCart.setImageResource(R.drawable.placeholder_buku)
                        }
                    })
            }

            btnDeleteCart.setOnClickListener {
                onHapusClick(buku)
            }
        }
    }

    override fun getItemCount(): Int = listBuku.size

    inner class ViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)
}