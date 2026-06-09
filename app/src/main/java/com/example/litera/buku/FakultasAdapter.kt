package com.example.litera.buku

import com.example.litera.R
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class FakultasAdapter(
    private val context: Context,
    private val listFakultas: List<String>,
    private val listJurusan: HashMap<String, List<String>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = listFakultas.size

    override fun getChildrenCount(groupPosition: Int): Int {
        return listJurusan[listFakultas[groupPosition]]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Any = listFakultas[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return listJurusan[listFakultas[groupPosition]]?.get(childPosition) ?: ""
    }

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    // Mengatur Tampilan Baris Fakultas (Induk)
// Mengatur Tampilan Baris Fakultas (Induk)
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val headerTitle = getGroup(groupPosition) as String

        // PERBAIKAN: Menambahkan 'android.R' langsung di depan layout bawaan Android
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false)

        // PERBAIKAN: Menambahkan 'android.R' langsung di depan id text1 bawaan Android
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = headerTitle
        textView.textSize = 16f
        textView.setTextColor(Color.BLACK)
        textView.setPadding(40, 30, 40, 30)

        return view
    }

    // Mengatur Tampilan Baris Jurusan di Dalamnya (Anak)
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val childText = getChild(groupPosition, childPosition) as String

        // PERBAIKAN: Menambahkan 'android.R' langsung di depan layout bawaan Android
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_single_choice, parent, false)

        // PERBAIKAN: Menambahkan 'android.R' langsung di depan id text1 bawaan Android
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = childText
        textView.textSize = 14f
        textView.setPadding(80, 25, 40, 25)

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}