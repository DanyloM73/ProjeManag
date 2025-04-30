package com.danylom73.projemanag.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    val title: String = "",
    val createdBy: String = "",
    var cards: ArrayList<Card> = ArrayList()
) : Parcelable
