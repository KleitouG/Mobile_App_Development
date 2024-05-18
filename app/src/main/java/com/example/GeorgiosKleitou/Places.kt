package com.example.GeorgiosKleitou

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Pois")

data class Pois(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String ?,
    val type: String ?,
    val description: String ?,
    var lat: Double,
    var lon: Double
)