package com.example.coroutines.free.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.coroutines.free.R
import com.example.coroutines.free.model.Article
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface ArticleLoader {
    suspend fun loadMore()
}

class ArticleAdapter(private val loader: ArticleLoader): RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {
    private val articles: MutableList<Article> = mutableListOf()
    private var loading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.article, parent, false) as ConstraintLayout
        val feed = layout.findViewById<TextView>(R.id.feed)
        val title = layout.findViewById<TextView>(R.id.title)
        val summary = layout.findViewById<TextView>(R.id.summary)
        return ViewHolder(layout, feed, title, summary)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]

        if(!loading && position >= articles.size -2) {
            Log.d("current position : ", position.toString())
            Log.d("loading true : ", articles.size.toString())
            loading = true

            GlobalScope.launch() {
                loader.loadMore()
                loading = false
            }
        }

        holder.feed.text = article.feed
        holder.title.text = article.title
        holder.summary.text = article.summary
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    fun add(articles: List<Article>) {
        this.articles.addAll(articles)
        notifyDataSetChanged()
    }

    class ViewHolder(
        val layout: ConstraintLayout,
        val feed: TextView,
        val title: TextView,
        val summary: TextView
    ): RecyclerView.ViewHolder(layout)
}