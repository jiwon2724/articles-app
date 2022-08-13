package com.example.coroutines.free.search

import android.util.Log
import com.example.coroutines.free.model.Article
import com.example.coroutines.free.model.Feed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class Searcher {
    private val dispatcher = newFixedThreadPoolContext(3, "IO-Search")
    private var factory = DocumentBuilderFactory.newInstance()

    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn", "http://rss.cnn.com/rss/cnn_topstories.rss"),
        Feed("fox", "http://feeds.foxnews.com/foxnews/politics?format=xml")
    )


    fun search(query: String): ReceiveChannel<Article> {
        val channel = Channel<Article>(150)
        feeds.forEach {
            GlobalScope.launch(dispatcher) {
                search(it, channel, query)
            }
        }
        return channel
    }

    /** Send **/
    private suspend fun search(
        feed: Feed,
        channel: SendChannel<Article>,
        query: String) {

        val builder = factory.newDocumentBuilder()
        val xml = builder.parse(feed.url)
        val news = xml.getElementsByTagName("channel").item(0)

        (0 until news.childNodes.length)
            .map { news.childNodes.item(it) }
            .filter { Node.ELEMENT_NODE == it.nodeType }
            .map { it as Element }
            .filter { "item" == it.tagName }
            .forEach {
                val title = it.getElementsByTagName("title")
                    .item(0)
                    .textContent

//                var summary = it.getElementsByTagName("description")
//                    .item(0)
//                    .textContent
//
//
//                Log.d("summary : ", summary.toString())
//
//                if(title.contains(query) || summary.contains(query)) {
//                    if (summary.contains("<div")) {
//                        summary = summary.substring(0, summary.indexOf("<div"))
//                    }
//
//                    val article = Article(feed.name, title, "test")
//                    channel.send(article)
//                }
                val article = Article(feed.name, title, "test")
                channel.send(article)
            }
    }
}