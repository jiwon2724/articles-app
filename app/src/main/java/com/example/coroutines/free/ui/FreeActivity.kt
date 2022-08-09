package com.example.coroutines.free.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.coroutines.free.R
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce

class FreeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_free)

        val context = newSingleThreadContext("myThread")

        val producer = GlobalScope.produce(context) {
            for(i in 0..9){
                send(i)
            }
        }


        runBlocking {
            producer.consumeEach {
                Log.d("it : ", it.toString())
            }
        }
    }
}