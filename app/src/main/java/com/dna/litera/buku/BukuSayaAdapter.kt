package com.dna.litera.buku

import android.graphics.Bitmap
import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.dna.litera.R
import com.dna.litera.data.BukuMilikSaya
import com.dna.litera.databinding.ItemBukuSayaBinding
import java.util.Locale

class BukuSayaAdapter(
    private var listBuku: List<BukuMilikSaya>,
    private val isHistory: Boolean,
    private val onPerpanjangClicked: (BukuMilikSaya) -> Unit
) : RecyclerView.Adapter<BukuSayaAdapter.ViewHolder>() {

    fun updateData(newList: List<BukuMilikSaya>) {
        listBuku = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBukuSayaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listBuku[position])
    }

    override fun getItemCount(): Int = listBuku.size

    inner class ViewHolder(private val binding: ItemBukuSayaBinding) : RecyclerView.ViewHolder(binding.root) {
        private var timer: CountDownTimer? = null

        fun bind(buku: BukuMilikSaya) {
            binding.tvJudulBukuSaya.text = buku.judul ?: "Tanpa Judul"
            binding.tvPenulisBukuSaya.text = buku.penulis ?: "Anonim"

            binding.root.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            binding.ivCoverBukuSaya.setImageResource(R.drawable.placeholder_buku)

            if (!buku.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .asBitmap()
                    .load(buku.imageUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            binding.ivCoverBukuSaya.setImageBitmap(resource)
                            Palette.from(resource).generate { palette ->
                                val warnaTerang = palette?.getLightMutedColor(Color.parseColor("#FFFFFF"))
                                    ?: Color.parseColor("#FFFFFF")
                                binding.root.setCardBackgroundColor(warnaTerang)
                            }
                        }
                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            binding.ivCoverBukuSaya.setImageResource(R.drawable.placeholder_buku)
                        }
                    })
            }

            timer?.cancel()

            if (isHistory) {
                binding.btnPerpanjang.visibility = View.VISIBLE
                binding.tvSisaHari.text = "Habis"
                binding.tvCountdown.text = "00:00:00"
                
                binding.tvSisaHari.setTextColor(Color.parseColor("#4CAF50"))
                binding.tvSisaHari.setBackgroundResource(R.drawable.bg_badge_green)
                binding.tvCountdown.setTextColor(Color.parseColor("#F44336"))
                binding.tvCountdown.setBackgroundResource(R.drawable.bg_badge_red)
                
                binding.btnPerpanjang.setOnClickListener {
                    onPerpanjangClicked(buku)
                }
            } else {
                binding.btnPerpanjang.visibility = View.GONE
                
                // LOGIKA: CEK STATUS PEMINJAMAN
                if (buku.statusPeminjaman == "MENUNGGU_KONFIRMASI" || buku.statusPeminjaman == "PENDING") {
                    // JIKA MASIH MENUNGGU: Tampilkan waktu penuh 24 jam dan jangan jalankan timer
                    binding.tvSisaHari.text = "1 Hari Lagi"
                    binding.tvCountdown.text = "24:00:00"
                    
                } else if (buku.statusPeminjaman == "DIPINJAM" || buku.statusPeminjaman == "AKTIF" || buku.statusPeminjaman == "ACTIVE") {
                    // JIKA SUDAH DIPINJAM/AKTIF: Jalankan hitung mundur berkurang
                    val waktuSekarang = System.currentTimeMillis()
                    val batasKembali = buku.tanggalKembali ?: 0L
                    val selisihWaktuMili = batasKembali - waktuSekarang

                    if (selisihWaktuMili <= 0) {
                        binding.tvSisaHari.text = "Habis"
                        binding.tvCountdown.text = "00:00:00"
                    } else {
                        val totalJam = selisihWaktuMili / (1000 * 60 * 60)
                        val totalHari = totalJam / 24

                        binding.tvSisaHari.text = if (totalHari > 0) "$totalHari Hari Lagi" else "Hari Ini"
                        
                        timer = object : CountDownTimer(selisihWaktuMili, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                val totalDetik = millisUntilFinished / 1000
                                val jam = (totalDetik / 3600)
                                val menit = (totalDetik % 3600) / 60
                                val detik = totalDetik % 60
                                binding.tvCountdown.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", jam, menit, detik)
                            }

                            override fun onFinish() {
                                binding.tvSisaHari.text = "Habis"
                                binding.tvCountdown.text = "00:00:00"
                            }
                        }.start()
                    }
                } else {
                    // Status lain (misal: default saat baru masuk halaman)
                    binding.tvSisaHari.text = "Menunggu"
                    binding.tvCountdown.text = "24:00:00"
                }

                binding.tvSisaHari.setTextColor(Color.parseColor("#4CAF50"))
                binding.tvSisaHari.setBackgroundResource(R.drawable.bg_badge_green)
                binding.tvCountdown.setTextColor(Color.parseColor("#F44336"))
                binding.tvCountdown.setBackgroundResource(R.drawable.bg_badge_red)
            }
        }
    }
}