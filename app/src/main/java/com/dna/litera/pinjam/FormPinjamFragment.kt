package com.dna.litera.pinjam

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dna.litera.data.Peminjaman
import com.dna.litera.R
import com.dna.litera.databinding.FragmentFormPinjamBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class FormPinjamFragment : Fragment(R.layout.fragment_form_pinjam) {

    private lateinit var binding: FragmentFormPinjamBinding

    // Inisialisasi Firebase Firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFormPinjamBinding.bind(view)

        // Ambil data tanggal pengambilan
        binding.etWaktuAmbil.setOnClickListener {
            val kalender = Calendar.getInstance()
            val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                binding.etWaktuAmbil.setText("$dayOfMonth/${month + 1}/$year")
            }, kalender.get(Calendar.YEAR), kalender.get(Calendar.MONTH), kalender.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        binding.btnBackForm.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnAjukanPeminjaman.setOnClickListener {
            val waktuAmbil = binding.etWaktuAmbil.text.toString().trim()

            if (waktuAmbil.isEmpty()) {
                Toast.makeText(context, "Silakan pilih waktu pengambilan dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Jalankan fungsi simpan ke Firestore
            simpanKeFirestore(waktuAmbil)
        }
    }

    private fun simpanKeFirestore(waktuAmbil: String) {
        // 1. Generate ID unik otomatis untuk dokumen transaksi peminjaman
        val refPeminjamanBaru = db.collection("peminjaman_buku").document()
        val idPeminjaman = refPeminjamanBaru.id

        // 2. Mockup data buku yang diambil dari keranjang belanja sebelumnya
        // (Nanti bagian ini disinkronkan dengan list asli dari file adapter Anda)
        val listBukuDipinjam = listOf(
            mapOf("idBuku" to "BK_001", "judul" to "Statistika dan Probabilitas")
        )

        // 3. Susun data berdasarkan blueprint Data Class Peminjaman
        val dataForm = Peminjaman(
            idPeminjaman = idPeminjaman,
            npmMahasiswa = binding.etNpmForm.text.toString(),
            namaMahasiswa = binding.etNamaForm.text.toString(),
            prodi = "Sistem Informasi",
            waktuPengambilan = waktuAmbil,
            durasiHari = 1,
            buktiKtmUrl = "https://storage.litera.id/ktm/dummy_ktm.jpg", // Nanti diganti url upload storage
            statusPeminjaman = "PENDING",
            daftarBuku = listBukuDipinjam
        )

        // 4. Perintah mendorong data masuk ke Firestore
        refPeminjamanBaru.set(dataForm)
            .addOnSuccessListener {
                // POP-UP PERTAMA: Berhasil mengajukan ke Firestore
                AlertDialog.Builder(requireContext())
                    .setTitle("Pengajuan Berhasil!")
                    .setMessage("Formulir telah diajukan ke database, silakan ambil bukunya ke perpustakaan.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()

                        Toast.makeText(context, "Menunggu konfirmasi admin (5 detik)...", Toast.LENGTH_SHORT).show()

                        // Handler Simulasi Admin Mengubah Status menjadi ACTIVE setelah 5 detik
                        Handler(Looper.getMainLooper()).postDelayed({
                            updateStatusMenjadiActive(idPeminjaman)
                        }, 5000)
                    }
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateStatusMenjadiActive(idDokumen: String) {
        val kalender = Calendar.getInstance()
        val tanggalMulai = kalender.timeInMillis

        // Tambahkan 1 hari untuk tanggal kembali
        kalender.add(Calendar.DAY_OF_YEAR, 1)
        val tanggalKembali = kalender.timeInMillis

        // Update field statusPeminjaman, tanggalMulai, dan tanggalKembali di Firestore secara real-time
        // Update field statusPeminjaman, tanggalMulai, dan tanggalKembali di Firestore secara real-time
        db.collection("peminjaman_buku").document(idDokumen)
            .update(
                "statusPeminjaman", "ACTIVE",
                "tanggalMulai", tanggalMulai,
                "tanggalKembali", tanggalKembali
            )
            .addOnSuccessListener {
                // POP-UP KEDUA: Muncul setelah status di database sukses berubah menjadi ACTIVE
                if (isAdded && context != null) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Peminjaman Buku Berhasil!")
                        .setMessage("Admin perpustakaan telah mengonfirmasi. Waktu hitung mundur peminjaman Anda resmi dimulai.")
                        .setPositiveButton("Lihat Buku Saya") { dialog, _ ->
                            dialog.dismiss()

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, BukuSayaFragment()) // PERBAIKAN: Ubah menjadi fragmentContainer
                                .commit()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
    }
}