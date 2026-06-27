package com.dna.litera.pinjam

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dna.litera.R
import com.dna.litera.data.KeranjangItem
import com.dna.litera.data.local.AppDatabase
import com.dna.litera.data.local.BukuCart
import com.dna.litera.databinding.FragmentDaftarPinjamBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DaftarPinjamFragment : Fragment(R.layout.fragment_daftar_pinjam) {

    private lateinit var binding: FragmentDaftarPinjamBinding
    private lateinit var keranjangAdapter: KeranjangAdapter
    private val listKeranjang = mutableListOf<KeranjangItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDaftarPinjamBinding.bind(view)

        setupRecyclerView()
        loadDataDariDatabase()

        binding.cbPilihSemua.setOnCheckedChangeListener { _, isChecked ->
            keranjangAdapter.pilihSemuaItem(isChecked)
        }

        binding.btnBackKeranjang.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnKonfirmasiPinjam.setOnClickListener {
            val total = listKeranjang.size
            if (total > 0) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, FormPinjamFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(context, "Pilih minimal 1 buku untuk dipinjam!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        keranjangAdapter = KeranjangAdapter(
            listKeranjang = listKeranjang,
            onJumlahChanged = {
                updateRingkasanTeks(listKeranjang.size)
            },
            onHapusItem = { posisi ->
                if (posisi in listKeranjang.indices) {
                    val itemDihapus = listKeranjang[posisi]
                    hapusDariDatabase(itemDihapus, posisi)
                }
            }
        )
        binding.rvKeranjangPinjam.layoutManager = LinearLayoutManager(context)
        binding.rvKeranjangPinjam.adapter = keranjangAdapter
    }

    private fun loadDataDariDatabase() {
        val database = AppDatabase.getDatabase(requireContext())
        val cartDao = database.bukuCartDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val dataCart = cartDao.ambilSemuaKeranjang()
            val convertedList = dataCart.map {
                KeranjangItem(
                    idBuku = it.id_buku,
                    judul = it.judul,
                    penulis = it.penulis,
                    imageUrl = it.imageUrl,
                    jumlahPinjam = 1
                )
            }

            withContext(Dispatchers.Main) {
                listKeranjang.clear()
                listKeranjang.addAll(convertedList)
                keranjangAdapter.notifyDataSetChanged()
                
                updateRingkasanTeks(listKeranjang.size)
                toggleEmptyState(listKeranjang.isEmpty())
            }
        }
    }

    private fun hapusDariDatabase(item: KeranjangItem, posisi: Int) {
        val database = AppDatabase.getDatabase(requireContext())
        val cartDao = database.bukuCartDao()

        lifecycleScope.launch(Dispatchers.IO) {
            cartDao.hapusDariKeranjang(
                BukuCart(
                    id_buku = item.idBuku,
                    judul = item.judul,
                    penulis = item.penulis,
                    imageUrl = item.imageUrl
                )
            )
            withContext(Dispatchers.Main) {
                listKeranjang.removeAt(posisi)
                keranjangAdapter.notifyItemRemoved(posisi)
                keranjangAdapter.notifyItemRangeChanged(posisi, listKeranjang.size)
                updateRingkasanTeks(listKeranjang.size)
                toggleEmptyState(listKeranjang.isEmpty())
            }
        }
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvKeranjangPinjam.visibility = View.GONE
            binding.layoutKeranjangKosong.visibility = View.VISIBLE
            binding.tvTotalItemHeader.text = "Keranjang kosong"
            binding.tvTotalItemBawah.text = ""
            binding.layoutStickyBottomCheckout.visibility = View.GONE
        } else {
            binding.rvKeranjangPinjam.visibility = View.VISIBLE
            binding.layoutKeranjangKosong.visibility = View.GONE
            binding.layoutStickyBottomCheckout.visibility = View.VISIBLE
        }
    }

    private fun updateRingkasanTeks(total: Int) {
        if (total > 0) {
            binding.tvTotalItemHeader.text = "$total buku terpilih"
            binding.tvTotalItemBawah.text = "$total Buku"
        }
    }
}