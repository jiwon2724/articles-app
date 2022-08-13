package com.example.coroutines.free.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coroutines.free.R
import com.example.coroutines.free.adapter.ArticleAdapter
import com.example.coroutines.free.databinding.ActivitySearchBinding
import com.example.coroutines.free.model.Article
import com.example.coroutines.free.search.Searcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

lateinit var binding: ActivitySearchBinding
class SearchActivity : AppCompatActivity() {
    private lateinit var viewAdapter: ArticleAdapter
    private val searcher = Searcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewAdapter = ArticleAdapter()

        binding.articleRv.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = viewAdapter
        }

        /** 검색 **/
        binding.button.setOnClickListener {
            viewAdapter.clear()
            GlobalScope.launch {
                search()
            }
        }
    }

    /** Receive **/
    private suspend fun search(){
        val query = binding.edit.text.toString()
        val channel = searcher.search(query)

        while(!channel.isClosedForReceive){
            val article = channel.receive()

            GlobalScope.launch(Dispatchers.Main) {
                viewAdapter.add(article)
            }
        }
    }
}