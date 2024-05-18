package com.example.GeorgiosKleitou

import androidx.room.*

@Dao
interface PoiDao{

    @Query("SELECT * FROM Pois WHERE id=:id")
    fun getPoisById(id: Long): Pois?

    @Query("SELECT * FROM Pois")
    fun getAllPois(): List<Pois>

    @Insert
    fun insert(pois: Pois) : Long
}