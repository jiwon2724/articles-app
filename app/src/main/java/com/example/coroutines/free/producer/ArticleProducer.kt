package com.example.coroutines.free.producer

import com.example.coroutines.free.model.Article
import com.example.coroutines.free.model.Feed
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

object ArticleProducer {
    private val dispatcher = newFixedThreadPoolContext(2, "IO")
    private var factory = DocumentBuilderFactory.newInstance()

    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn", "http://rss.cnn.com/rss/cnn_topstories.rss"),
        Feed("fox", "http://feeds.foxnews.com/foxnews/politics?format=xml")
    )

    val producer = GlobalScope.produce(dispatcher) {
        feeds.forEach {
            send(fetchArticles(it))
        }
    }

    private fun fetchArticles(feed: Feed): List<Article> {
        val builder = factory.newDocumentBuilder()
        val xml = builder.parse(feed.url)
        val news = xml.getElementsByTagName("channel").item(0)

        return (0 until news.childNodes.length)
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
}