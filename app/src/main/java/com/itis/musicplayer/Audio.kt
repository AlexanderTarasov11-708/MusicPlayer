package com.itis.musicplayer

import java.io.Serializable

class Audio(var data: String?, var title: String?, val album: String, val artist: String, val albumId: Long?) : Serializable
