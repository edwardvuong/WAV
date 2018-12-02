package com.example.ryan.wav6;

public class Song
{
    private long id;
    private String title;
    private String artist;

    public Song(long songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }
    public long getID(){return id;} //CAN ADD MORE VARIABLES
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
}
