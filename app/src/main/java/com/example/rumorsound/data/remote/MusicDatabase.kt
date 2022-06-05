package com.example.rumorsound.data.remote

import android.util.Log
import com.example.rumorsound.domain.entities.Song
import com.example.rumorsound.other.Constants.MAIN_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class MusicDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val mainCollection = firestore.collection(MAIN_COLLECTION)

    suspend fun getMainCollection(): List<Song> {
        return try {
            mainCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception) {
            Log.d("MusicLoad", "ErrorDownloading: ${e.message}")
            emptyList()
        }
    }

}