package com.example.resultviewer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream


class ShowResult : AppCompatActivity() {
    var pos: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show)
        pos = intent.getIntExtra("RESULTID", 0)
        var tweetButton = findViewById<Button>(R.id.tweetButtonId)
        tweetButton.setOnClickListener {
            shareButton()
        }
        var editButton = findViewById<Button>(R.id.editButtonId)
        editButton.setOnClickListener {
            val intentShow = Intent(this, EditResult::class.java)
            intentShow.putExtra("RESULTID", pos)
            startActivity(intentShow)
        }
        var deleteButton = findViewById<Button>(R.id.deleteButtonId)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.deleteDialog_title)
                .setMessage(R.string.deleteDialog_text)
                .setPositiveButton("OK") { dialog, which ->
                    var newList: Array<Result> = arrayOf()
                    var counter: Int = 0
                    var oldList: Array<Result> = Gson().fromJson(
                        readFiles("resultData.txt") ?: "[]",
                        object : TypeToken<Array<Result?>?>() {}.type
                    )
                    oldList.forEach {
                        if (counter != pos) newList += it
                        counter++
                    }
                    val str = Gson().toJson(newList)
                    saveFile("resultData.txt", str)
                    finish()
                }
                .setNegativeButton("No") { dialog, which -> }
                .show()
        }
    }

    private fun shareButton() {
        var resultList: Array<Result> = Gson().fromJson(
            readFiles("resultData.txt") ?: "[]",
            object : TypeToken<Array<Result?>?>() {}.type
        )
        val result = resultList[pos]
        val builder = ShareCompat.IntentBuilder.from(this)
        builder.setChooserTitle(getString(R.string.chooseApp_text))
        if(result.sub != null){
            builder.setText(result.title + " " + result.diff + " " + result.target)
        }
        else{
            builder.setText(result.title + " " + result.diff + " " + result.target + " " + result.sub)
        }
        builder.setType("image/jpg")
        builder.addStream(Uri.parse(result.image))
        builder.startChooser()
    }

    override fun onResume() {
        super.onResume()
        drawView()
    }

    private fun drawView(){
        var resultList: Array<Result> = Gson().fromJson(
            readFiles("resultData.txt") ?: "[]",
            object : TypeToken<Array<Result?>?>() {}.type
        )
        val result = resultList[pos]
        var inputStream: InputStream?
        var image: Bitmap
        try {
            inputStream = contentResolver?.openInputStream(Uri.parse(result.image))
            image = BitmapFactory.decodeStream(inputStream)
        }
        catch(e: FileNotFoundException){
            Toast.makeText(applicationContext, R.string.showresult_errortoast, Toast.LENGTH_LONG).show()
            image = BitmapFactory.decodeResource(getResources(), android.R.drawable.editbox_dropdown_light_frame)
        }
        val imageView = findViewById<ImageView>(R.id.showImageId)
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
        imageView.setImageBitmap(image)
        findViewById<TextView>(R.id.showTitleId).text =
            (result.title + " " + result.diff + " " + result.target + " " + result.sub)
        findViewById<TextView>(R.id.showGameId).text = result.game
        findViewById<TextView>(R.id.showDateId).text = dateToString(result.date)
    }

    private fun saveFile(file: String, str: String) {
        applicationContext.openFileOutput(file, Context.MODE_PRIVATE).use {
            it.write(str.toByteArray())
        }
    }

    private fun readFiles(file: String): String? {
        val readFile = File(applicationContext.filesDir, file)
        if (!readFile.exists()) {
            Log.d("DEBUG", "No file exists")
            return null
        } else {
            return readFile.bufferedReader().use(BufferedReader::readText)
        }
    }
}