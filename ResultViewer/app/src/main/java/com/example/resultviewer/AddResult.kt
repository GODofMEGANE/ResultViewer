package com.example.resultviewer

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AddResult : AppCompatActivity() {
    var result = Result(0, Calendar.getInstance(), "", "", "", "", "", "")
    var targetSpinnerItems: Array<String> = arrayOf()
    var diffSpinnerItems: Array<String> = arrayOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addresult)
        findViewById<EditText>(R.id.dateFieldId).setText(
            dateToString(result.date)
        )
        var gameSpinnerItems =
            arrayOf("beatmania IIDX", "BMS", "chunithm", "maimai", "オンゲキ", "sound voltex")
        var gameSpinner = findViewById<Spinner>(R.id.gameSpinnerId)
        gameSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, gameSpinnerItems)
        gameSpinner.setSelection(
            getSharedPreferences(
                "DataStore",
                Context.MODE_PRIVATE
            ).getInt("SelectedGame", 0)
        )
        gameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                findViewById<EditText>(R.id.gameFieldId).setText(item)
                when (position) {
                    0 -> {
                        targetSpinnerItems = arrayOf(
                            "EASY",
                            "CLEAR",
                            "HARD",
                            "EX-HARD",
                            "FC",
                            "PERFECT",
                            "A",
                            "AA",
                            "AAA",
                            "MAX",
                            "合格"
                        )
                        diffSpinnerItems = arrayOf("(A)", "(L)", "(H)", "(E)")
                    }
                    1 -> {
                        targetSpinnerItems = arrayOf(
                            "EASY",
                            "CLEAR",
                            "HARD",
                            "EX-HARD",
                            "FC",
                            "PERFECT",
                            "A",
                            "AA",
                            "AAA",
                            "MAX",
                            "合格"
                        )
                        diffSpinnerItems = arrayOf("★", "☆", "★★", "st", "sl", "▼", "▽")
                    }
                    2 -> {
                        targetSpinnerItems = arrayOf("S", "SS", "SSS", "FC", "AJ", "AJC", "合格")
                        diffSpinnerItems = arrayOf("MASTER", "EXPERT", "ADVANCED", "BASIC")
                    }
                    3 -> {
                        targetSpinnerItems =
                            arrayOf("S", "S+", "SS", "SS+", "SSS", "SSS+", "FC", "AP", "AP+")
                        diffSpinnerItems =
                            arrayOf("MASTER", "Re:MASTER", "EXPERT", "ADVANCED", "BASIC")
                    }
                    4 -> {
                        targetSpinnerItems =
                            arrayOf("S", "SS", "SSS", "SSS+", "FC", "AB", "FB", "ABFB", "AB+")
                        diffSpinnerItems =
                            arrayOf("MASTER", "LUNATIC", "EXPERT", "ADVANCED", "BASIC")
                    }
                    5 -> {
                        targetSpinnerItems = arrayOf(
                            "COMP",
                            "HARD",
                            "UC",
                            "PUC",
                            "AA",
                            "AA+",
                            "AAA",
                            "AAA+",
                            "S",
                            "合格"
                        )
                        diffSpinnerItems = arrayOf("MXM", "EXH", "HVN", "GRV", "INF", "ADV", "NOV")
                    }
                }
                val editor = getSharedPreferences("DataStore", Context.MODE_PRIVATE).edit()
                editor.putInt("SelectedGame", position)
                editor.apply()
                reloadSpinner()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        var selectImageButton = findViewById<Button>(R.id.selectImageId)
        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, READ_REQUEST_CODE)
        }
        var dateButton = findViewById<Button>(R.id.dataInvButtonId)
        dateButton.setOnClickListener {
            val dtp = DatePickerDialog(this,DatePickerDialog.OnDateSetListener{view,y,m,d ->
                result.date.set(y, m, d)
                findViewById<EditText>(R.id.dateFieldId).setText(
                    dateToString(result.date)
                )
            }, result.date.get(Calendar.YEAR),result.date.get(Calendar.MONTH),result.date.get(Calendar.DAY_OF_MONTH)
            )
            dtp.show()
        }
        var cancelButton = findViewById<Button>(R.id.cancelId)
        cancelButton.setOnClickListener {
            finish()
        }
        var saveButton = findViewById<Button>(R.id.saveId)
        saveButton.setOnClickListener {
            result.title = findViewById<EditText>(R.id.titleFieldId).text.toString()
            result.game = findViewById<EditText>(R.id.gameFieldId).text.toString()
            result.diff = findViewById<EditText>(R.id.diffFieldId).text.toString()
            result.target = findViewById<EditText>(R.id.targetFieldId).text.toString()
            result.sub = findViewById<EditText>(R.id.subFieldId).text.toString()
            var data: Array<Result>? = Gson().fromJson(
                readFiles("resultData.txt") ?: "[]",
                object : TypeToken<Array<Result?>?>() {}.type
            )
            if(data != null){
                result.id = data.size
            }else{
                result.id = 0
            }
            data?.let {
                data = data!! + result
            }
            data ?: run {
                data = arrayOf(result)
            }
            val str = Gson().toJson(data)
            saveFile("resultData.txt", str)
            finish()
        }
    }

    companion object {
        private const val READ_REQUEST_CODE: Int = 42
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            READ_REQUEST_CODE -> {
                try {
                    resultData?.data?.also { uri ->
                        result.image = uri.toString()
                        val inputStream = contentResolver?.openInputStream(uri)
                        val image = BitmapFactory.decodeStream(inputStream)
                        val imageView = findViewById<ImageView>(R.id.selectedImageId)
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageView.setImageBitmap(image)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveFile(file: String, str: String) {

        applicationContext.openFileOutput(file, Context.MODE_PRIVATE).use {
            it.write(str.toByteArray())
        }

        //File(applicationContext.filesDir, file).writer().use {
        //    it.write(str)
        //}
    }

    private fun readFiles(file: String): String? {

        // to check whether file exists or not
        val readFile = File(applicationContext.filesDir, file)

        if (!readFile.exists()) {
            Log.d("DEBUG", "No file exists")
            return null
        } else {
            return readFile.bufferedReader().use(BufferedReader::readText)
        }
    }

    private fun reloadSpinner() {
        var targetSpinner = findViewById<Spinner>(R.id.targetSpinnerId)
        var diffSpinner = findViewById<Spinner>(R.id.diffSpinnerId)
        targetSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, targetSpinnerItems)
        diffSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, diffSpinnerItems)
        targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                findViewById<EditText>(R.id.targetFieldId).setText(item)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        diffSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                findViewById<EditText>(R.id.diffFieldId).setText(item)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}