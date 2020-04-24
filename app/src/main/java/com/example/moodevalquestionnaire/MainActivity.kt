package com.example.moodevalquestionnaire

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.Date.from

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val network = Network()
        val localDateYesterday = LocalDate.now().minusDays(2)
        val yesterday:Date = getYesterday()

        val date: Date = SimpleDateFormat("yyyy-MM-dd").parse("2020-04-15")
        network.queryDayByDate(date) { lambResponse ->
            val response = lambResponse?.moodStr.toString()
            Log.i("MAIN", "query result = $response")
            response
        }

        checkYesterday(network, yesterday)

        button_submit.setOnClickListener {
            val id: Int = mood_choice.checkedRadioButtonId
            if (id != -1) {
                val mood: RadioButton = findViewById(id)
                val moodId = moodTextToEnum(mood.text as String)

                network.createDay(yesterday, moodId)
                Toast.makeText(this@MainActivity, "You chose ${mood.text}: $moodId.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moodTextToEnum(mood: String): Int {
        var enum = -1
        when (mood) {
            "Terrible" -> enum = 1
            "Bad" -> enum = 2
            "Normal" -> enum = 3
            "Good" -> enum = 4
            "Marvelous" -> enum = 5
        }
        return enum
    }

    private fun checkYesterday(network: Network, yesterday: Date) {
        network.queryDayByDate(yesterday) { lambResponse ->
            if (lambResponse != null) {
                val topMessage = findViewById<TextView>(R.id.top_message)
                topMessage.text = "You have already entered data for yesterday."
            }
            "Complete"
        }
    }

    private fun getYesterday(): Date {
        val localDateYesterday = LocalDate.now().minusDays(2)
        return from(localDateYesterday.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }
}
