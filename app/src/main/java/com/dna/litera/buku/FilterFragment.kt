package com.dna.litera.buku

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.dna.litera.R
import com.dna.litera.databinding.FragmentFilterBinding

class FilterFragment : Fragment(R.layout.fragment_filter) {

    private lateinit var binding: FragmentFilterBinding
    private var selectedCategory: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFilterBinding.bind(view)

        binding.btnBackFromFilter.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.rgKategoriFilterOptions.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = view.findViewById<RadioButton>(checkedId)
            selectedCategory = radioButton.text.toString()
            
            // Tampilkan tombol ceklis (Apply) jika ada pilihan
            binding.btnApplyFilter.visibility = View.VISIBLE
        }

        binding.btnApplyFilter.setOnClickListener {
            selectedCategory?.let { category ->
                // Kirim hasil filter kembali ke SearchFragment
                setFragmentResult("filterKey", bundleOf("selectedCategory" to category))
                parentFragmentManager.popBackStack()
            }
        }
    }
}