package com.example.coroutines.free

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coroutines.free.adapter.ArticleAdapter
import com.example.coroutines.free.databinding.ActivityMainBinding
import com.example.coroutines.free.model.Article
import com.example.coroutines.free.model.Feed
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.lang.UnsupportedOperationException
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    private val dispatcher = newFixedThreadPoolContext(2, "IO")
    private var factory = DocumentBuilderFactory.newInstance()
    lateinit var binding: ActivityMainBinding

    private lateinit var viewAdapter: ArticleAdapter

    var feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn", "http://rss.cnn.com/rss/cnn_topstories.rss"),
        Feed("fox", "http://feeds.foxnews.com/foxnews/politics?format=xml"),
        Feed("inv", "htt:myNewsFeed")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewAdapter = ArticleAdapter()

        binding.articles.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = viewAdapter
        }

        hideSystemUI()
        asyncLoadNews()
    }

    private fun asyncFetchArticles(feed: Feed, dispatcher: CoroutineDispatcher) =

        GlobalScope.async(dispatcher) {
            delay(1000)

            val builder = factory.newDocumentBuilder()
            val xml = builder.parse(feed.url)
            val news = xml.getElementsByTagName("channel").item(0)

            (0 until news.childNodes.length)
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .map {
                    val title = it.getElementsByTagName("title")
                        .item(0)
                        .textContent
                    val summary = it.getElementsByTagName("description")
                        .item(0)
                        .textContent
                    Article(feed.name, title, summary)
                }
        }

    /** 여러개의 피드를 다 가지고 올 때 까지 대기 후 병합 **/
    private fun asyncLoadNews() = GlobalScope.launch() {
        val requests = mutableListOf<Deferred<List<Article>>>()

        feeds.mapTo(requests){
            asyncFetchArticles(it, dispatcher)
        }

        // 피드 수
        requests.forEach {
            it.join()
        }

        /**
         *  await()
         *  코루틴이 완료될 때 까지 대기
         *
         *  join()
         *  join을 사용해 디퍼드를 기다리면 대기할 때 예외가 전파되지 않는다.
         *  요청을 읽을 땐 예외를 전파한다.
         * **/

        // 뉴스 해드라인 수
        val articles = requests
            .filter { !it.isCancelled }
            .flatMap { it.getCompleted() }

        val failed = requests
            .filter { it.isCancelled }
            .size

        launch(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            viewAdapter.add(articles)
        }
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
}