package com.pashto.quranicgrammar.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pashto.quranicgrammar.R
import com.pashto.quranicgrammar.data.model.Surah
import com.pashto.quranicgrammar.ui.adapter.AyahAdapter

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SURAH = "EXTRA_SURAH"
    }

    private lateinit var ayahAdapter: AyahAdapter
    private lateinit var rvAyahs: RecyclerView
    private var surah: Surah? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        @Suppress("DEPRECATION")
        surah = intent.getSerializableExtra(EXTRA_SURAH) as? Surah

        val toolbar: Toolbar = findViewById(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (surah == null) {
            finish()
            return
        }

        supportActionBar?.title = surah?.nameArabic
        supportActionBar?.subtitle = surah?.namePashto

        setupHeaderViews()
        setupAyahRecyclerView()
    }

    private fun setupHeaderViews() {
        val tvHeaderArabic: TextView = findViewById(R.id.tvHeaderArabic)
        val tvHeaderPashto: TextView = findViewById(R.id.tvHeaderPashto)
        val tvHeaderInfo: TextView = findViewById(R.id.tvHeaderInfo)
        val tvSurahSummary: TextView = findViewById(R.id.tvSurahSummary)
        val tvBismillah: TextView = findViewById(R.id.tvBismillah)

        surah?.let { s ->
            tvHeaderArabic.text = s.nameArabic
            tvHeaderPashto.text = s.namePashto
            tvHeaderInfo.text = "شمېره: ${s.number} • ${s.revelationPashto} • ${s.ayahCount} آيتونه"
            tvSurahSummary.text = s.summaryPashto

            if (s.number == 1) {
                tvBismillah.visibility = View.GONE
            } else {
                tvBismillah.visibility = View.VISIBLE
                tvBismillah.text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
            }
        }
    }

    private fun setupAyahRecyclerView() {
        rvAyahs = findViewById(R.id.rvAyahs)
        rvAyahs.layoutManager = LinearLayoutManager(this)
        surah?.ayahs?.let { ayahsList ->
            ayahAdapter = AyahAdapter(ayahsList)
            rvAyahs.adapter = ayahAdapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}