package com.dna.litera.riwayat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dna.litera.data.RiwayatItem
import com.dna.litera.databinding.ItemRiwayatBukuBinding // Sesuaikan nama binding XML item riwayat Anda

class RiwayatAdapter(private var listRiwayat: List<RiwayatItem>) :
    RecyclerView.Adapter<RiwayatAdapter.RiwayatViewHolder>() {

    inner class RiwayatViewHolder(val binding: ItemRiwayatBukuBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
        val binding = ItemRiwayatBukuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RiwayatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
        val riwayat = listRiwayat[position]

        with(holder.binding) {
            tvJudulRiwayat.text = riwayat.judulBuku
            tvStatusSelesai.text = if (riwayat.statusPinjam == "Dipinjam") {
                "Pinjam: ${riwayat.tanggalPinjam}"
            } else {
                "Kembali: ${riwayat.tanggalKembali}"
            }

            // Memberikan warna teks teks status secara dinamis berdasarkan status peminjaman
            if (riwayat.statusPinjam == "Dipinjam") {
                tvStatusSelesai.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark))
            } else {
                tvStatusSelesai.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
            }
        }
    }

    override fun getItemCount(): Int = listRiwayat.size

    fun updateData(newList: List<RiwayatItem>) {
        this.listRiwayat = newList
        notifyDataSetChanged()
    }
}