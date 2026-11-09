package com.pashto.quranicgrammar.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pashto.quranicgrammar.R
import com.pashto.quranicgrammar.data.model.Ayah

class AyahAdapter(
    private var ayahs: List<Ayah>
) : RecyclerView.Adapter<AyahAdapter.AyahViewHolder>() {

    private val expandedStates = mutableMapOf<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AyahViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ayah, parent, false)
        return AyahViewHolder(view)
    }

    override fun onBindViewHolder(holder: AyahViewHolder, position: Int) {
        val ayah = ayahs[position]
        val isExpanded = expandedStates[ayah.ayahNumber] ?: true
        holder.bind(ayah, isExpanded)
    }

    override fun getItemCount(): Int = ayahs.size

    inner class AyahViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAyahBadge: TextView = itemView.findViewById(R.id.tvAyahBadge)
        private val tvArabicText: TextView = itemView.findViewById(R.id.tvArabicText)
        private val tvPashtoTranslation: TextView = itemView.findViewById(R.id.tvPashtoTranslation)
        private val tvOverallTarkib: TextView = itemView.findViewById(R.id.tvOverallTarkib)
        private val rvWordGrammar: RecyclerView = itemView.findViewById(R.id.rvWordGrammar)
        private val btnToggleIrab: Button = itemView.findViewById(R.id.btnToggleIrab)

        fun bind(ayah: Ayah, isExpanded: Boolean) {
            tvAyahBadge.text = "آیت ${ayah.ayahNumber}"
            tvArabicText.text = "${ayah.textArabic} ﴿${ayah.ayahNumber}﴾"
            tvPashtoTranslation.text = ayah.textPashto
            tvOverallTarkib.text = "عمومي نحوي جوړښت: ${ayah.overallTarkibPashto}"

            rvWordGrammar.layoutManager = LinearLayoutManager(itemView.context)
            rvWordGrammar.isNestedScrollingEnabled = false
            rvWordGrammar.adapter = WordIrabAdapter(ayah.words)

            rvWordGrammar.visibility = if (isExpanded) View.VISIBLE else View.GONE
            btnToggleIrab.text = if (isExpanded) "د کلمو اعراب پټ کړئ ▲" else "د کلمو پښتو اعراب وښایاست ▼"

            btnToggleIrab.setOnClickListener {
                expandedStates[ayah.ayahNumber] = !isExpanded
                notifyItemChanged(adapterPosition)
            }
        }
    }
}