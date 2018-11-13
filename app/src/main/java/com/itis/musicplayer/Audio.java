package com.itis.musicplayer;

import java.io.Serializable;

public class Audio implements Serializable {

    private String data;
    private String title;
    private String album;
    private String artist;

    private Long albumId;

    public Audio(String data, String title, String album, String artist, Long albumId) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.albumId = albumId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public Long getAlbumId() {
        return albumId;
    }

}
