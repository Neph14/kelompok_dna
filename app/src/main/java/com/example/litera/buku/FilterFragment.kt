package com.example.litera.buku

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.example.litera.R
import com.example.litera.databinding.FragmentFilterBinding

class FilterFragment : Fragment(R.layout.fragment_filter) {

    private lateinit var binding: FragmentFilterBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFilterBinding.bind(view)

        binding.btnBackFromFilter.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.rgKategoriFilterOptions.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = view.findViewById<RadioButton>(checkedId)
            val selectedCategory = radioButton.text.toString()
            
            // Kirim hasil filter kembali ke SearchFragment
            setFragmentResult("filterKey", bundleOf("selectedCategory" to selectedCategory))
            parentFragmentManager.popBackStack()
        }
    }
}