package com.example.moodevalquestionnaire

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        button_submit.setOnClickListener {
            val id: Int = mood_choice.checkedRadioButtonId
            if (id != -1) {
                val mood: RadioButton = findViewById(id)
                Toast.makeText(this@MainActivity, "You chose ${mood.text}.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
