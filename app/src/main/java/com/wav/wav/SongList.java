package com.wav.wav;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;

import com.wav.wav.MusicService.MusicBinder;

public class SongList extends AppCompatActivity implements Serializable {
    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        getSongList();

        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getAlbum().compareTo(b.getAlbum());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
    }

    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        startActivity(new Intent(SongList.this, MainActivity.class));

//        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra("MusicService", (Serializable) musicSrv);
//        intent.putExtra("Bound", (Serializable) musicBound);
//        startActivity(intent);

        //String intentData = gson.toJson()

    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        //menu item selected
//        switch (item.getItemId()) {
//            case R.id.action_shuffle:
//                //shuffle
//                break;
//            case R.id.action_end:
//                stopService(playIntent);
//                musicSrv=null;
//                System.exit(0);
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onDestroy() {
        if (musicBound) unbindService(musicConnection);
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        // iterate over the results, first checking that we have valid data:
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum));
            }
            while (musicCursor.moveToNext());
        }
    }

}


//import android.Manifest;
//import android.content.ContentResolver;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.net.Uri;
//import android.provider.MediaStore;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
//import android.widget.Toast;
//
//import java.util.ArrayList;
//
//public class SongList extends AppCompatActivity {
//
//
//    private static final int MY_PERMISSIONS_REQUEST = 1;
//
//    ArrayList<String> arrayList;
//
//    ListView listView;
//
//    ArrayAdapter<String> adapter;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_song_list);
//
//        if(ContextCompat.checkSelfPermission(SongList.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            if(ActivityCompat.shouldShowRequestPermissionRationale(SongList.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                ActivityCompat.requestPermissions(SongList.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
//
//            } else {
//                ActivityCompat.requestPermissions(SongList.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
//
//            }
//
//        } else {
//            doStuff();
//        }
//    }
//
//    public void doStuff() {
//        listView = (ListView) findViewById(R.id.listView);
//        arrayList = new ArrayList<>();
//        getMusic();
//        adapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, arrayList);
//        listView.setAdapter((adapter));
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                // open music player to play desired song
//            }
//        });
//
//    }
//
//    public void getMusic() {
//        ContentResolver contentResolver = getContentResolver();
//        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);
//
//        if(songCursor != null && songCursor.moveToFirst()) {
//            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
//            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
//            int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
//
//            do {
//                String currentTitle = songCursor.getString(songTitle);
//                String currentArtist = songCursor.getString(songArtist);
//                String currentLocation = songCursor.getString(songLocation);
//
//                arrayList.add("Title: " + currentTitle + "\n"
//                        + "Artist: " + currentArtist
//                + "Location: " + currentLocation);
//            } while (songCursor.moveToNext());
//
//        }
//    }
//
//    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST: {
//                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if (ContextCompat.checkSelfPermission(SongList.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                        Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
//
//                        doStuff();
//                    }
//
//                } else {
//                    Toast.makeText(this, "No permission granted!", Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//                return;
//            }
//        }
//    }
//}
