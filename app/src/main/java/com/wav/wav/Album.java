package com.wav.wav;

public class Album {
    private long id;
    private String mAlbum;

    public Album(long songID, String album) {
        id=songID;
        mAlbum = album;
    }

    public long getID(){return id;}
    public String getAlbum() {return mAlbum;}
}
