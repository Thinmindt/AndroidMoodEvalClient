package com.example.moodevalquestionnaire

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.example.moodevalquestionnaire.type.CustomType
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val GRAPHQL_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val customTypeAdapterDate = object: CustomTypeAdapter<Date> {
            override fun encode(value: Date): CustomTypeValue<*> {
                return CustomTypeValue.GraphQLString(GRAPHQL_DATE_FORMAT.format(value))
            }
            override fun decode(value: CustomTypeValue<*>): Date {
                return try {
                    GRAPHQL_DATE_FORMAT.parse(value.value.toString())
                } catch (e: ParseException) {
                    throw RuntimeException(e)
                }
            }
        }

        val apolloClient = ApolloClient.builder()
            .serverUrl("http://192.168.50.9:8080/graphql")
            .okHttpClient(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
            )
            .addCustomTypeAdapter(CustomType.NAIVEDATE, customTypeAdapterDate)
            .build()


        val date: Date = SimpleDateFormat("yyyy-MM-dd").parse("2020-04-20")
        val dayByDateQuery = DayByDateQuery(date)

        val tag = "Main Activity"

        val callbacks = object: ApolloCall.Callback<DayByDateQuery.Data>() {
            public override fun onResponse(response: Response<DayByDateQuery.Data>) {
                Log.i(tag, "Got ${response.data?.getDayByDate?.moodStr.toString()}.")
            }

            public override fun onFailure(e: ApolloException) {
                Log.i(tag, "Error: ${e.message}")
            }
        }

        apolloClient.query(
            dayByDateQuery
        )
            .enqueue(callbacks)

        button_submit.setOnClickListener {
            val id: Int = mood_choice.checkedRadioButtonId
            if (id != -1) {
                val mood: RadioButton = findViewById(id)
                Toast.makeText(this@MainActivity, "You chose ${mood.text}.", Toast.LENGTH_SHORT).show()

            }
        }
    }
}
