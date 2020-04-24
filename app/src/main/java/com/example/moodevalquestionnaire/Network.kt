package com.example.moodevalquestionnaire

import android.content.Context
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.example.moodevalquestionnaire.type.CustomType
import com.example.moodevalquestionnaire.type.NewDay
import okhttp3.OkHttpClient
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Network {
    private val GRAPHQL_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val TAG = "NETWORK"

    var apolloClient: ApolloClient

    init {
        apolloClient = setApolloClient()
    }

    data class ResponseString(var result: String? = null)

    fun queryDayByDate(date: Date, responseCallback: (response: DayByDateQuery.GetDayByDate?) -> String ): String? {
        var queryResponse: String? = null
        val dayByDateQuery = DayByDateQuery(date)
        val callbacks = object: ApolloCall.Callback<DayByDateQuery.Data>() {
            override fun onResponse(response: Response<DayByDateQuery.Data>) {
                Log.i(TAG, "Got ${response.data?.getDayByDate?.moodStr.toString()}.")
                queryResponse = responseCallback(response.data?.getDayByDate)
            }

            override fun onFailure(e: ApolloException) {
                Log.i(TAG, "Error: ${e.message}")
            }
        }
        apolloClient.query(
            dayByDateQuery
        )
            .enqueue(callbacks)
        return queryResponse
    }

    fun createDay(date: Date, mood: Int?) {
        val moodInput: Input<Int> = Input.fromNullable(mood)
        Log.i(TAG, "Mood Input = ${moodInput.value}, Mood = $mood")

        //Check if day exists
        queryDayByDate(date) { response ->
            Log.i(TAG, "Mood value for date given = ${response?.moodStr.toString()}")
            // It doesn't exist
            if (response == null) {
                val data = NewDay(date = date, moodId = moodInput)
                val createDay = CreateDayMutation(data)

                val callbacks = object: ApolloCall.Callback<CreateDayMutation.Data>() {
                    override fun onResponse(response: Response<CreateDayMutation.Data>) {
                        Log.i(TAG, "Got ${response.data?.createDay?.moodStr.toString()}.")
                    }

                    override fun onFailure(e: ApolloException) {
                        Log.i(TAG, "Error: ${e.message}")
                    }
                }

                apolloClient.mutate(createDay)
                    .enqueue(callbacks)
            }
            "Complete"
        }
    }

    private fun setApolloClient (): ApolloClient {
        val customTypeAdapterDate = object : CustomTypeAdapter<Date> {
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

        return ApolloClient.builder()
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
    }
}