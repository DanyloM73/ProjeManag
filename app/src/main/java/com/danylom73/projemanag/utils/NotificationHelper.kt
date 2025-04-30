package com.danylom73.projemanag.utils

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class NotificationHelper {
    fun sendNotification(
        userId: String,
        messageTitle: String,
        messageBody: String
    ) {
        val client = OkHttpClient()

        val json = """
        {
            "userIds": ["$userId"],
            "title": "$messageTitle",
            "body": "$messageBody"
        }
        """.trimIndent()

        val requestBody = json.toRequestBody(
            "application/json; charset=utf-8".toMediaType()
        )

        val request = Request.Builder()
            .url("http://192.168.1.101:3000/send-notification")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NOTIFICATION", "Failed to send: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("NOTIFICATION", "Notification sent successfully")
                } else {
                    Log.e("NOTIFICATION", "Error: ${response.code}")
                }
            }
        })
    }
}