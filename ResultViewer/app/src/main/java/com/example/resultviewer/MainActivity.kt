package com.example.resultviewer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File

data class Result(
    var image: String,
    var title: String,
    var game: String,
    var diff: String,
    var target: String,
    var sub: String
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sortSpinnerItems = arrayOf(
            getString(R.string.sortByDate_text),
            getString(R.string.sortByTitle_text),
            getString(R.string.sortByGame_text)
        )
        var sortSpinner = findViewById<Spinner>(R.id.sortSpinnerId)
        sortSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, sortSpinnerItems)
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                drawView()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        var addButton = findViewById<FloatingActionButton>(R.id.addButtonId)
        addButton.setOnClickListener {
            val intentAdd = Intent(this, AddResult::class.java)
            startActivity(intentAdd)
        }
        var mainList = findViewById<ListView>(R.id.mainListId)
        mainList.setOnItemClickListener { parent, view, position, id ->
            val intentShow = Intent(this, ShowResult::class.java)
            intentShow.putExtra("RESULTID", position)
            startActivity(intentShow)
        }
    }

    override fun onResume() {
        super.onResume()
        drawView()
    }

    fun drawView() {
        var resultList: Array<Result> = Gson().fromJson(
            readFiles("resultData.txt") ?: "[]",
            object : TypeToken<Array<Result?>?>() {}.type
        )
        when (findViewById<Spinner>(R.id.sortSpinnerId).selectedItemPosition) {
            0 -> {
            }
            1 -> {
                resultList.sortBy { it.title }
            }
            2 -> {
                resultList.sortBy { it.game }
            }
        }
        var viewList: Array<String> = arrayOf()
        resultList.forEach {
            viewList += (it.title + " " + it.diff + " " + it.target + " " + it.sub + " " + it.game)
        }
        val mainList = findViewById<ListView>(R.id.mainListId)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            viewList ?: arrayOf("")
        )
        mainList.adapter = adapter
    }

    private fun readFiles(file: String): String? {
        val readFile = File(applicationContext.filesDir, file)
        if (!readFile.exists()) {
            Log.d("MYDEBUG", "No file exists")
            return null
        } else {
            return readFile.bufferedReader().use(BufferedReader::readText)
        }
    }
}