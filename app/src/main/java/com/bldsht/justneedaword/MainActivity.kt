package com.bldsht.justneedaword

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bldsht.justneedaword.databinding.ActivityMainBinding
import com.bldsht.justneedaword.responsemodel.WordResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var adapter: MeaningAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isAmoledThemeEnabled = sharedPreferences.getBoolean("amoled_theme", false)

        if (isDarkTheme()) {
            setTheme(R.style.AppTheme_Amoled)
        } else {
            setTheme(R.style.AppTheme)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.searchBtn.setOnClickListener {
            val word = binding.searchInput.text.toString()
            getMeaning(word)
        }

        binding.searchInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                event?.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER) {
                binding.searchBtn.performClick()
                true
            } else {
                false
            }
        }

        adapter = MeaningAdapter(emptyList())
        binding.meaningRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.meaningRecyclerView.adapter = adapter

    }
    private fun isDarkTheme(): Boolean {
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun getMeaning(word: String) {
        setInProgress(true)
        GlobalScope.launch {
            try {
                val response = RetrofitCall.dictionaryApi.getMeaning(word)
                if (response.body() == null) {
                    throw (Exception())
                }
                runOnUiThread {
                    setInProgress(false)
                    response.body()?.first()?.let {
                        setUI(it)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    setInProgress(false)
                    Toast.makeText(applicationContext, "No Word Found", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun setUI(response: WordResult) {
        if (response.sourceUrls!!.isEmpty()) {
            binding.sourceLinkTv.visibility = View.GONE
            binding.sourceLink.visibility = View.GONE
        } else {
            binding.sourceLinkTv.visibility = View.VISIBLE
            binding.sourceLink.visibility = View.VISIBLE

            val urls = response.sourceUrls.joinToString(",\n")
            val spannableString = SpannableString(urls)

            // Apply spans to each URL
            var startIndex = 0
            response.sourceUrls.forEach { url ->
                val endIndex = startIndex + url.length
                spannableString.setSpan(UnderlineSpan(), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableString.setSpan(ForegroundColorSpan(getColor(R.color.blue)), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableString.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            "http://$url" // Add http:// prefix if missing
                        } else {
                            url

                        }

                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
                        startActivity(intent)
                    }
                }, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                startIndex = endIndex + 2 // account for ",\n"
            }

            binding.sourceLinkTv.text = spannableString
            binding.sourceLinkTv.movementMethod = LinkMovementMethod.getInstance()
        }

        binding.wordTextview.text = response.word
        binding.phoneticTextview.text = response.phonetic
        adapter.updateNewData(response.meanings)

    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.sourceLink.visibility = View.INVISIBLE
            binding.searchBtn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.sourceLink.visibility = View.GONE
            binding.searchBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}