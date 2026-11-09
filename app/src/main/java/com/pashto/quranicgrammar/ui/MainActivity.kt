package com.pashto.quranicgrammar.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.pashto.quranicgrammar.R
import com.pashto.quranicgrammar.data.model.QuranDatabase
import com.pashto.quranicgrammar.data.model.Surah
import com.pashto.quranicgrammar.data.repository.QuranRepository
import com.pashto.quranicgrammar.ui.adapter.SurahAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var repository: QuranRepository
    private lateinit var surahAdapter: SurahAdapter
    private lateinit var rvSurahs: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatusInfo: TextView
    private lateinit var llEmptyState: View
    private var currentDatabase: QuranDatabase? = null
    private var currentQuery: String = ""

    private val remoteSyncUrl = "https://example.com/api/juz_amma.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "د عامه جزء پښتو اعراب"
        supportActionBar?.subtitle = "د قرآني عربي د نحو او اعراب زده کړه"

        rvSurahs = findViewById(R.id.rvSurahs)
        progressBar = findViewById(R.id.progressBar)
        tvStatusInfo = findViewById(R.id.tvStatusInfo)
        llEmptyState = findViewById(R.id.llEmptyState)

        repository = QuranRepository(this)
        setupRecyclerView()
        loadLocalDatabase()
    }

    private fun setupRecyclerView() {
        surahAdapter = SurahAdapter(
            surahs = emptyList(),
            onSurahClick = { surah -> navigateToDetail(surah) },
            onFilterResult = { filteredCount ->
                llEmptyState.visibility = if (filteredCount == 0) View.VISIBLE else View.GONE
                val total = currentDatabase?.surahs?.size ?: 0
                if (currentQuery.isNotEmpty()) {
                    tvStatusInfo.text = "پایلې: ${filteredCount} له ${total} سورتونو څخه"
                }
            }
        )
        rvSurahs.layoutManager = LinearLayoutManager(this)
        rvSurahs.adapter = surahAdapter
    }

    private fun loadLocalDatabase() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val db = repository.getDatabase()
                currentDatabase = db
                surahAdapter.updateData(db.surahs)
                updateStatusHeader(db)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "تېروتنه: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateStatusHeader(db: QuranDatabase) {
        val storageSource = if (repository.hasCustomCachedDatabase()) {
            "تازه شوې داخلي حافظه (V${db.version})"
        } else {
            "محلي آفلاین اسټس (V${db.version})"
        }
        tvStatusInfo.text = "مجموعه: ${db.surahs.size} سورتونه • $storageSource"
    }

    private fun navigateToDetail(surah: Surah) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_SURAH, surah)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.apply {
            queryHint = getString(R.string.search_hint)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    currentQuery = query ?: ""
                    surahAdapter.filter.filter(query)
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    currentQuery = newText ?: ""
                    surahAdapter.filter.filter(newText)
                    return true
                }
            })
            searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean = true
                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    currentQuery = ""
                    surahAdapter.filter.filter("")
                    llEmptyState.visibility = View.GONE
                    currentDatabase?.let { updateStatusHeader(it) }
                    return true
                }
            })
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_update_db -> {
                promptUpdateDatabase()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun promptUpdateDatabase() {
        AlertDialog.Builder(this)
            .setTitle("د ډیټابیس آنلاین تازه کول")
            .setMessage("ایا غواړئ د نویو څېړنو فایل له سرور څخه کښته او په داخلي حافظه کې خوندي کړئ؟")
            .setPositiveButton("هو، تازه یې کړه") { _, _ -> performRemoteSync() }
            .setNegativeButton("لغوه", null)
            .show()
    }

    private fun performRemoteSync() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = repository.updateDatabaseFromRemote(remoteSyncUrl)
            progressBar.visibility = View.GONE
            result.onSuccess { updatedDb ->
                currentDatabase = updatedDb
                surahAdapter.updateData(updatedDb.surahs)
                updateStatusHeader(updatedDb)
                Snackbar.make(rvSurahs, "ډیټابیس تازه شو! (نسخه ${updatedDb.version})", Snackbar.LENGTH_LONG).show()
            }.onFailure { error ->
                Snackbar.make(rvSurahs, "تازه کول ناکام شول: ${error.localizedMessage}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}