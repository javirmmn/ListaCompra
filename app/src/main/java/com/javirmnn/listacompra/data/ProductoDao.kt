package com.javirmnn.listacompra.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    // Devuelve los productos ordenados alfabéticamente sin importar mayúsculas
    @Query("SELECT * FROM productos ORDER BY nombre COLLATE NOCASE ASC")
    fun obtenerTodos(): Flow<List<Producto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: Producto)

    @Update
    suspend fun actualizar(producto: Producto)

    @Delete
    suspend fun borrar(producto: Producto)
}