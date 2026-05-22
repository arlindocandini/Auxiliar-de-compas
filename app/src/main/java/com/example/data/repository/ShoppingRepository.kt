package com.example.data.repository

import com.example.data.dao.ShoppingDao
import com.example.data.entities.ShoppingItem
import com.example.data.entities.ShoppingTrip
import kotlinx.coroutines.flow.Flow

class ShoppingRepository(private val shoppingDao: ShoppingDao) {
    val allTrips: Flow<List<ShoppingTrip>> = shoppingDao.getAllTrips()

    suspend fun getTrip(tripId: Long): ShoppingTrip? = shoppingDao.getTripById(tripId)

    fun getItemsForTrip(tripId: Long): Flow<List<ShoppingItem>> = shoppingDao.getItemsForTrip(tripId)

    suspend fun getItemsForTripSync(tripId: Long): List<ShoppingItem> = shoppingDao.getItemsForTripSync(tripId)

    suspend fun insertTrip(trip: ShoppingTrip): Long = shoppingDao.insertTrip(trip)

    suspend fun updateTrip(trip: ShoppingTrip) = shoppingDao.updateTrip(trip)

    suspend fun insertItem(item: ShoppingItem): Long = shoppingDao.insertItem(item)

    suspend fun updateItem(item: ShoppingItem) = shoppingDao.updateItem(item)

    suspend fun deleteItemById(itemId: Long) = shoppingDao.deleteItemById(itemId)

    suspend fun deleteTripWithItems(tripId: Long) = shoppingDao.deleteTripWithItems(tripId)
}
