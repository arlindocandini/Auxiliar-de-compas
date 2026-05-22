package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entities.ShoppingItem
import com.example.data.entities.ShoppingTrip
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_trips ORDER BY date DESC")
    fun getAllTrips(): Flow<List<ShoppingTrip>>

    @Query("SELECT * FROM shopping_trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): ShoppingTrip?

    @Query("SELECT * FROM shopping_items WHERE tripId = :tripId ORDER BY name ASC")
    fun getItemsForTrip(tripId: Long): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE tripId = :tripId")
    suspend fun getItemsForTripSync(tripId: Long): List<ShoppingItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: ShoppingTrip): Long

    @Update
    suspend fun updateTrip(trip: ShoppingTrip)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem): Long

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Long)

    @Query("DELETE FROM shopping_trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: Long)

    @Query("DELETE FROM shopping_items WHERE tripId = :tripId")
    suspend fun deleteItemsForTrip(tripId: Long)

    @Transaction
    suspend fun deleteTripWithItems(tripId: Long) {
        deleteItemsForTrip(tripId)
        deleteTrip(tripId)
    }
}
