package com.example.litera.riwayat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.litera.R
import com.google.firebase.firestore.FirebaseFirestore

class RiwayatPinjamFragment : Fragment(R.layout.fragment_riwayat) {

    private val db = FirebaseFirestore.getInstance()
    private val npmUser = "0621101011" // Dummy NPM login saat ini

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Contoh query mengambil riwayat peminjaman yang sudah selesai
        db.collection("peminjaman_buku")
            .whereEqualTo("npmMahasiswa", npmUser)
            .whereEqualTo("statusPeminjaman", "RETURNED") // Mencari data yang sudah berstatus kembali
            .get()
            .addOnSuccessListener { dokumen ->
                if (!dokumen.isEmpty) {
                    // Masukkan data ke dalam Adapter RecyclerView Riwayat Anda
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal memuat riwayat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}