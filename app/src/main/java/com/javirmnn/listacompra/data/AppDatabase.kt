package com.javirmnn.listacompra.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Producto::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
}