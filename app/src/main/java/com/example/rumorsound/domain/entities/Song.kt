package com.example.rumorsound.domain.entities

/** Сущность, с которой мы работаем. Объект песни. */
data class Song (
    val mediaId: Int = 0,
    val title: String = "",
    val subtitle: String = "###",
    val url: String = "",
//    val album: String = "",
//    val albumArtUrl: String = "",
//    val artist: String = "",
) {
    companion object {
        const val UNDEFINED_ID = 0
    }
}

