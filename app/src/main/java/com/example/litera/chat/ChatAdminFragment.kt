package com.example.litera.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.litera.R
import com.example.litera.databinding.FragmentChatAdminBinding

class ChatAdminFragment : Fragment(R.layout.fragment_chat_admin) {

    private lateinit var binding: FragmentChatAdminBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatAdminBinding.bind(view)

        binding.btnBackChat.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnKirimPesan.setOnClickListener {
            val pesan = binding.etIsiPesan.text.toString().trim()
            if (pesan.isNotEmpty()) {
                // Tambah pesan user ke layar (Simulasi)
                Toast.makeText(context, "Pesan terkirim: $pesan", Toast.LENGTH_SHORT).show()
                binding.etIsiPesan.text.clear()

                // Simulasi Admin Membalas Otomatis dalam 2 detik
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isAdded) {
                        Toast.makeText(context, "Admin Nevan: Tentu, dengan senang hati akan kami bantu terkait kendala Anda.", Toast.LENGTH_LONG).show()
                    }
                }, 2000)
            }
        }
    }
}