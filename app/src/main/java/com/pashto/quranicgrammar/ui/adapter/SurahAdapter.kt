package com.pashto.quranicgrammar.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pashto.quranicgrammar.R
import com.pashto.quranicgrammar.data.model.Surah
import java.util.Locale
import java.util.regex.Pattern

class SurahAdapter(
    private var surahs: List<Surah>,
    private val onSurahClick: (Surah) -> Unit,
    private val onFilterResult: ((filteredCount: Int) -> Unit)? = null
) : RecyclerView.Adapter<SurahAdapter.SurahViewHolder>(), Filterable {

    private var fullList: List<Surah> = ArrayList(surahs)
    private var displayedList: List<Surah> = ArrayList(surahs)

    fun updateData(newSurahs: List<Surah>) {
        this.fullList = ArrayList(newSurahs)
        this.displayedList = ArrayList(newSurahs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_surah, parent, false)
        return SurahViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurahViewHolder, position: Int) {
        holder.bind(displayedList[position])
    }

    override fun getItemCount(): Int = displayedList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.trim() ?: ""
                val filtered = if (query.isEmpty()) {
                    fullList
                } else {
                    val normalizedQuery = normalize(query)
                    fullList.filter { surah ->
                        surah.number.toString().contains(query) ||
                        normalize(surah.nameArabic).contains(normalizedQuery) ||
                        normalize(surah.namePashto).contains(normalizedQuery) ||
                        surah.nameEnglish.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))
                    }
                }
                return FilterResults().apply {
                    values = filtered
                    count = filtered.size
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                displayedList = (results?.values as? List<Surah>) ?: fullList
                notifyDataSetChanged()
                onFilterResult?.invoke(displayedList.size)
            }
        }
    }

    private fun normalize(input: String): String {
        val tashkeel = Pattern.compile("[\u064B-\u065F\u0670\u06D6-\u06ED]")
        var text = tashkeel.matcher(input).replaceAll("")
        text = text.replace(Regex("[أإآٱ]"), "ا")
        text = text.replace("ة", "ه")
        text = text.replace(Regex("[ىيېۍئ]"), "ي")
        text = text.replace("ؤ", "و")
        return text.trim().lowercase(Locale.ROOT)
    }

    inner class SurahViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSurahNumber: TextView = itemView.findViewById(R.id.tvSurahNumber)
        private val tvNameArabic: TextView = itemView.findViewById(R.id.tvNameArabic)
        private val tvNamePashto: TextView = itemView.findViewById(R.id.tvNamePashto)
        private val tvRevelationType: TextView = itemView.findViewById(R.id.tvRevelationType)
        private val tvAyahCount: TextView = itemView.findViewById(R.id.tvAyahCount)

        fun bind(surah: Surah) {
            tvSurahNumber.text = surah.number.toString()
            tvNameArabic.text = surah.nameArabic
            tvNamePashto.text = surah.namePashto
            tvRevelationType.text = surah.revelationPashto
            tvAyahCount.text = "${surah.ayahCount} آيتونه"
            itemView.setOnClickListener { onSurahClick(surah) }
        }
    }
}