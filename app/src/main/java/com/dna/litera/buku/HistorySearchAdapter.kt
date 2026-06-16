package com.dna.litera.buku

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dna.litera.R

class HistorySearchAdapter(
    private var listHistory: List<String>,
    private val onHistoryClicked: (String) -> Unit
) : RecyclerView.Adapter<HistorySearchAdapter.ViewHolder>() {

    fun updateData(newList: List<String>) {
        listHistory = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHistoryText: TextView = view.findViewById(R.id.tvHistoryText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = listHistory[position]
        holder.tvHistoryText.text = history
        holder.itemView.setOnClickListener { onHistoryClicked(history) }
    }

    override fun getItemCount(): Int = listHistory.size
}