package wav.wav;


import android.content.pm.PackageManager;
import android.os.Build;
import 	android.Manifest;

import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;


import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;

import wav.wav.MusicService.MusicBinder;


import android.widget.MediaController.MediaPlayerControl;


public class MainActivity extends AppCompatActivity {

    protected ArrayList<Song> songList;
    protected ListView songView;

    protected MusicService musicSrv;
    protected Intent playIntent;
    protected boolean musicBound=false;

    protected MusicController controller;

    protected boolean paused=false, playbackPaused=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
// app-defined int constant

                return;
            }}


        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        getSongList();

        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
    }



    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, sortOrder);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);


            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

           int albumArtIDColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                int thisDuration = musicCursor.getInt(durationColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                long thisAlbumArtID = musicCursor.getLong(albumArtIDColumn);

                songList.add(new Song(thisId, thisTitle, thisArtist, thisDuration, thisAlbum, getCoverArtPath(musicResolver, thisAlbumArtID)));
            }
            while (musicCursor.moveToNext());
            musicCursor.close();;
        }

    }


    private static String getCoverArtPath( ContentResolver musicResolver, long androidAlbumId) {
        String path = null;
        Cursor c = musicResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{Long.toString(androidAlbumId)},
                null);
        if (c != null) {
            if (c.moveToFirst()) {
                path = c.getString(0);
            }
            c.close();
        }
        return path;
    }

    public void songPicked(View view){
     startActivity(new Intent(MainActivity.this, Playing.class).putExtra("SetSong", Integer.toString(Integer.parseInt(view.getTag().toString()))).putExtra("songList", songList));

    }




    public void toNowPlaying (View view) {
        startActivity(new Intent(MainActivity.this, Playing.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));


    }





}