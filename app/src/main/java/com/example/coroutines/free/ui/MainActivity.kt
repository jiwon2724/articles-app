package com.example.coroutines.free.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coroutines.free.adapter.ArticleAdapter
import com.example.coroutines.free.adapter.ArticleLoader
import com.example.coroutines.free.databinding.ActivityMainBinding
import com.example.coroutines.free.model.Article
import com.example.coroutines.free.model.Feed
import com.example.coroutines.free.producer.ArticleProducer
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity(), ArticleLoader {
    lateinit var binding: ActivityMainBinding

    private lateinit var viewAdapter: ArticleAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewAdapter = ArticleAdapter()

        binding.articles.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = viewAdapter
        }

        GlobalScope.launch {
            loadMore()
        }

        hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override suspend fun loadMore() {
        val producer = ArticleProducer.producer

        Log.d("producerState : ", producer.isClosedForReceive.toString())

        if(!producer.isClosedForReceive){
            val articles = producer.receive()

            Log.d("article!! : ", articles.toString())

            GlobalScope.launch(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}