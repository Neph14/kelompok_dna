package com.dna.litera.buku

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dna.litera.R
import com.dna.litera.data.Buku
import com.dna.litera.data.local.AppDatabase
import com.dna.litera.data.local.BukuCart
import com.dna.litera.databinding.FragmentSearchBinding
import com.dna.litera.home.BukuVertikalAdapter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var binding: FragmentSearchBinding
    private val db = FirebaseFirestore.getInstance()
    private val allBooksList = mutableListOf<Buku>()
    private lateinit var resultAdapter: BukuVertikalAdapter
    private lateinit var historyAdapter: HistorySearchAdapter
    private var searchHistory = mutableListOf<String>()
    private var isFilteringByCategory = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        loadSearchHistory()
        setupAdapters()
        setupListeners()
        fetchAllBooks()
        loadSuggestedBooks()
        
        // Listen for filter result
        setFragmentResultListener("filterKey") { _, bundle ->
            val selectedCategory = bundle.getString("selectedCategory")
            if (selectedCategory != null) {
                isFilteringByCategory = true
                applyCategoryFilter(selectedCategory)
            }
        }
    }

    private fun setupAdapters() {
        val onBukuClicked: (Buku) -> Unit = { buku ->
            saveSearchQuery(buku.judul ?: "")
            val intent = Intent(requireContext(), DetailBukuActivity::class.java)
            intent.putExtra("EXTRA_BUKU", buku)
            startActivity(intent)
        }

        val onPlusClicked: (Buku) -> Unit = { buku ->
            tambahBukuKeKeranjang(buku)
        }

        resultAdapter = BukuVertikalAdapter(emptyList(), onBukuClicked, onPlusClicked)
        
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = resultAdapter

        binding.rvSuggestedBooks.layoutManager = LinearLayoutManager(requireContext())
        
        historyAdapter = HistorySearchAdapter(searchHistory) { historyText ->
            binding.etSearchBox.setText(historyText)
            handleSearchQuery(historyText)
            saveSearchQuery(historyText)
        }
        binding.rvRecentSearches.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentSearches.adapter = historyAdapter
        
        updateHistoryVisibility()
    }

    private fun tambahBukuKeKeranjang(buku: Buku) {
        // CEK STATUS KETERSEDIAAN
        if (!buku.status.equals("Tersedia", ignoreCase = true)) {
            Toast.makeText(requireContext(), "Buku ini lagi kosong, coba lain waktu", Toast.LENGTH_SHORT).show()
            return
        }

        val database = AppDatabase.getDatabase(requireContext())
        val cartDao = database.bukuCartDao()

        val itemBaru = BukuCart(
            id_buku = buku.judul ?: "", 
            judul = buku.judul ?: "Tanpa Judul",
            penulis = buku.penulis ?: "Anonim",
            imageUrl = buku.imageUrl ?: ""
        )

        lifecycleScope.launch(Dispatchers.IO) {
            cartDao.tambahKeKeranjang(itemBaru)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "'${buku.judul}' berhasil ditambah ke keranjang!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnOpenFilter.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FilterFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.ivSearchIconInside.setOnClickListener {
            val query = binding.etSearchBox.text.toString().trim()
            if (query.isNotEmpty()) {
                saveSearchQuery(query)
                handleSearchQuery(query)
            }
        }

        binding.btnClearSearchText.setOnClickListener {
            binding.etSearchBox.text.clear()
        }

        binding.btnBackFromSearch.setOnClickListener {
            resetSearchState()
        }

        binding.etSearchBox.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearchBox.text.toString().trim()
                if (query.isNotEmpty()) {
                    saveSearchQuery(query)
                }
                true
            } else {
                false
            }
        }

        binding.etSearchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFilteringByCategory) return
                val query = s.toString().trim()
                handleSearchQuery(query)
            }
        })
    }

    private fun handleSearchQuery(query: String) {
        if (query.isEmpty()) {
            binding.btnClearSearchText.visibility = View.GONE
            binding.scrollInitialSearch.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
            binding.tvSearchNoResult.visibility = View.GONE
            binding.btnBackFromSearch.visibility = View.GONE
        } else {
            binding.btnClearSearchText.visibility = View.VISIBLE
            binding.scrollInitialSearch.visibility = View.GONE
            binding.btnBackFromSearch.visibility = View.VISIBLE
            filterBooks(query)
        }
    }

    private fun filterBooks(query: String) {
        val filtered = allBooksList.filter { 
            it.judul?.contains(query, ignoreCase = true) == true || 
            it.penulis?.contains(query, ignoreCase = true) == true 
        }

        if (filtered.isEmpty()) {
            binding.rvSearchResults.visibility = View.GONE
            binding.tvSearchNoResult.visibility = View.VISIBLE
            binding.tvSearchNoResult.text = "Tidak ada hasil"
        } else {
            binding.tvSearchNoResult.visibility = View.GONE
            binding.rvSearchResults.visibility = View.VISIBLE
            resultAdapter.updateData(filtered)
        }
    }

    private fun applyCategoryFilter(category: String) {
        binding.etSearchBox.setText(category)
        binding.etSearchBox.isEnabled = false
        binding.btnBackFromSearch.visibility = View.VISIBLE
        binding.scrollInitialSearch.visibility = View.GONE
        binding.btnClearSearchText.visibility = View.GONE
        
        // Sembunyikan elemen history secara eksplisit
        binding.layoutRecentSearch.visibility = View.GONE
        
        val filtered = allBooksList.filter { it.tipe?.equals(category, ignoreCase = true) == true }
        
        if (filtered.isEmpty()) {
            binding.rvSearchResults.visibility = View.GONE
            binding.tvSearchNoResult.visibility = View.VISIBLE
            binding.tvSearchNoResult.text = "buku untuk kategori ini sedang kosong, coba beberapa waktu lagi"
            
            // Sembunyikan kembali scrollInitialSearch untuk memastikan tampilan bersih
            binding.scrollInitialSearch.visibility = View.GONE
        } else {
            binding.tvSearchNoResult.visibility = View.GONE
            binding.rvSearchResults.visibility = View.VISIBLE
            resultAdapter.updateData(filtered)
        }
    }

    private fun resetSearchState() {
        isFilteringByCategory = false
        binding.etSearchBox.text.clear()
        binding.etSearchBox.isEnabled = true
        binding.btnBackFromSearch.visibility = View.GONE
        binding.scrollInitialSearch.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.GONE
        binding.tvSearchNoResult.visibility = View.GONE
        
        // Tampilkan kembali daftar rekomendasi
        binding.rvSuggestedBooks.visibility = View.VISIBLE
        binding.layoutRecentSearch.visibility = if (searchHistory.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadSuggestedBooks() {
        binding.shimmerSearch.startShimmer()
        binding.shimmerSearch.visibility = View.VISIBLE
        binding.scrollInitialSearch.visibility = View.GONE

        val sharedPref = requireActivity().getSharedPreferences("SesiLitera", android.content.Context.MODE_PRIVATE)
        val npmUser = sharedPref.getString("LOGIN_NPM", "") ?: ""

        if (npmUser.isEmpty()) {
            loadDefaultSuggestions()
            return
        }

        // Ambil kategori dari buku yang pernah/sedang dipinjam
        db.collection("peminjaman")
            .whereEqualTo("npm", npmUser)
            .get()
            .addOnSuccessListener { documents ->
                val categories = documents.mapNotNull { it.getString("tipeBuku") }.distinct()
                
                if (categories.isEmpty()) {
                    loadDefaultSuggestions()
                } else {
                    fetchSuggestionsByCategory(categories)
                }
            }
            .addOnFailureListener {
                loadDefaultSuggestions()
            }
    }

    private fun fetchSuggestionsByCategory(categories: List<String>) {
        db.collection("koleksi_buku")
            .whereIn("tipe", categories)
            .limit(10) // Ambil lebih banyak untuk diacak
            .get()
            .addOnSuccessListener { documents ->
                val allSuggested = documents.map { doc ->
                    Buku(
                        judul = doc.getString("judul"),
                        penulis = doc.getString("penulis"),
                        imageUrl = doc.getString("imageUrl"),
                        tipe = doc.getString("tipe"),
                        status = doc.getString("status"),
                        deskripsi = doc.getString("deskripsi")
                    )
                }.shuffled().take(2) // Maksimal 2 buku

                if (allSuggested.isEmpty()) {
                    loadDefaultSuggestions()
                } else {
                    binding.rvSuggestedBooks.adapter = BukuVertikalAdapter(allSuggested, { buku ->
                        val intent = Intent(requireContext(), DetailBukuActivity::class.java)
                        intent.putExtra("EXTRA_BUKU", buku)
                        startActivity(intent)
                    }, { buku ->
                        tambahBukuKeKeranjang(buku)
                    })
                    
                    binding.shimmerSearch.stopShimmer()
                    binding.shimmerSearch.visibility = View.GONE
                    
                    // Hanya tampilkan jika sedang tidak mencari/filter
                    if (!isFilteringByCategory && binding.etSearchBox.text.isEmpty()) {
                        binding.scrollInitialSearch.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun loadDefaultSuggestions() {
        db.collection("koleksi_buku").limit(2).get()
            .addOnSuccessListener { documents ->
                val suggestions = documents.map { doc ->
                    Buku(
                        judul = doc.getString("judul"),
                        penulis = doc.getString("penulis"),
                        imageUrl = doc.getString("imageUrl"),
                        tipe = doc.getString("tipe"),
                        status = doc.getString("status"),
                        deskripsi = doc.getString("deskripsi")
                    )
                }
                binding.rvSuggestedBooks.adapter = BukuVertikalAdapter(suggestions, { buku ->
                    val intent = Intent(requireContext(), DetailBukuActivity::class.java)
                    intent.putExtra("EXTRA_BUKU", buku)
                    startActivity(intent)
                }, { buku ->
                    tambahBukuKeKeranjang(buku)
                })

                binding.shimmerSearch.stopShimmer()
                binding.shimmerSearch.visibility = View.GONE
                
                // Hanya tampilkan jika sedang tidak mencari/filter
                if (!isFilteringByCategory && binding.etSearchBox.text.isEmpty()) {
                    binding.scrollInitialSearch.visibility = View.VISIBLE
                }
            }
    }

    private fun saveSearchQuery(query: String) {
        if (query.isEmpty()) return
        
        // Remove if exists to bring to front
        searchHistory.remove(query)
        searchHistory.add(0, query)
        
        if (searchHistory.size > 5) searchHistory.removeAt(5)
        
        val sharedPref = requireActivity().getSharedPreferences("SearchHistory", android.content.Context.MODE_PRIVATE)
        sharedPref.edit().putStringSet("history", searchHistory.toMutableSet()).apply()
        
        historyAdapter.updateData(ArrayList(searchHistory))
        updateHistoryVisibility()
    }

    private fun loadSearchHistory() {
        val sharedPref = requireActivity().getSharedPreferences("SearchHistory", android.content.Context.MODE_PRIVATE)
        val historySet = sharedPref.getStringSet("history", emptySet()) ?: emptySet()
        // Use a list to maintain order (though StringSets don't maintain order)
        searchHistory = historySet.toMutableList()
    }

    private fun updateHistoryVisibility() {
        if (searchHistory.isEmpty()) {
            binding.layoutRecentSearch.visibility = View.GONE
        } else {
            binding.layoutRecentSearch.visibility = View.VISIBLE
        }
    }

    private fun fetchAllBooks() {
        db.collection("koleksi_buku").get()
            .addOnSuccessListener { documents ->
                allBooksList.clear()
                for (doc in documents) {
                    allBooksList.add(Buku(
                        judul = doc.getString("judul"),
                        penulis = doc.getString("penulis"),
                        imageUrl = doc.getString("imageUrl"),
                        tipe = doc.getString("tipe"),
                        status = doc.getString("status"),
                        deskripsi = doc.getString("deskripsi")
                    ))
                }
            }
    }
}