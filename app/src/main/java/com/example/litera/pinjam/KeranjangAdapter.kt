package com.example.litera.pinjam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.litera.R
import com.example.litera.data.KeranjangItem
import com.example.litera.databinding.ItemBukuKeranjangBinding

class KeranjangAdapter(
    private var listKeranjang: MutableList<KeranjangItem>, // Diubah ke MutableList agar fleksibel dengan Fragment
    private val onJumlahChanged: () -> Unit,
    private val onHapusItem: (Int) -> Unit
) : RecyclerView.Adapter<KeranjangAdapter.KeranjangViewHolder>() {

    // Set untuk menyimpan ID atau objek KeranjangItem mana saja yang sedang dicentang oleh user
    private val listItemDicentang = mutableSetOf<KeranjangItem>()

    class KeranjangViewHolder(val binding: ItemBukuKeranjangBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeranjangViewHolder {
        val binding = ItemBukuKeranjangBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KeranjangViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeranjangViewHolder, position: Int) {
        val item = listKeranjang[position]

        with(holder.binding) {
            tvJudulKeranjang.text = item.judul
            tvPenulisKeranjang.text = item.penulis
            tvJumlahBuku.text = item.jumlahPinjam.toString()

            // 1. Logika Sinkronisasi Checkbox per item buku
            // Mengatur apakah checkbox di layar harus tercentang atau tidak berdasarkan data set pencatat
            cbItemKeranjang.setOnCheckedChangeListener(null) // Reset listener sejenak agar tidak looping eror
            cbItemKeranjang.isChecked = listItemDicentang.contains(item)

            cbItemKeranjang.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    listItemDicentang.add(item)
                } else {
                    listItemDicentang.remove(item)
                }
                onJumlahChanged() // Picu fragment untuk menghitung ulang total di bawah bar sticky
            }

            // Load cover buku di keranjang
            Glide.with(root.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder_cover)
                .into(ivCoverKeranjang)

            // Logika Tombol Plus (+)
            btnTambahBuku.setOnClickListener {
                item.jumlahPinjam++
                tvJumlahBuku.text = item.jumlahPinjam.toString()
                onJumlahChanged()
            }

            // Logika Tombol Minus (-)
            btnKurangBuku.setOnClickListener {
                if (item.jumlahPinjam > 1) {
                    item.jumlahPinjam--
                    tvJumlahBuku.text = item.jumlahPinjam.toString()
                    onJumlahChanged()
                }
            }

            // Logika Tombol Hapus / Sampah
            btnHapusKeranjang.setOnClickListener {
                listItemDicentang.remove(item) // Hapus dari daftar centang jika barangnya dibuang
                onHapusItem(position)
            }
        }
    }

    override fun getItemCount(): Int = listKeranjang.size

    fun updateData(newList: ArrayList<KeranjangItem>) {
        this.listKeranjang = newList
        notifyDataSetChanged()
    }

    // =========================================================================
    // FUNGSI PERBAIKAN BARU YANG DICARI OLEH DAFTARPINJAMFRAGMENT:
    // =========================================================================

    // Fungsi untuk menghitung berapa item buku yang sedang dicentang saat ini
    fun hitungTotalTerpilih(): Int {
        return listItemDicentang.size
    }

    // Fungsi untuk mencentang semua item sekaligus atau membatalkan semua centang dari sticky bar
    fun pilihSemuaItem(isPilihSemua: Boolean) {
        listItemDicentang.clear()
        if (isPilihSemua) {
            listItemDicentang.addAll(listKeranjang)
        }
        notifyDataSetChanged() // Gambar ulang semua checkbox di layar RecyclerView
        onJumlahChanged() // Update angka total teks di bar bawah fragment
    }
}