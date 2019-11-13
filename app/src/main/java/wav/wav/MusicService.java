package wav.wav;

import android.app.Service;


import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;


public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {


    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;


    //music queue
    //private Queue<Song> queue = new LinkedList<Song>();

    private final IBinder musicBind = new MusicBinder();
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;


    private boolean shuffle = false;
    private Random rand;

    private boolean prepared = false;

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }


    public void onCreate() {
        //create the service
        super.onCreate();
        //initialize position
        songPosn = 0;
        //create player
        player = new MediaPlayer();
        initMusicPlayer();
        rand = new Random();


    }

    public void initMusicPlayer() {
        //set player properties

        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }


    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong() {
        //play a song

        player.reset();


        //get song
        Song playSong = songs.get(songPosn);


        songTitle = playSong.getTitle();

        //get id
        long currSong = playSong.getID();
        //set uri

        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);


        try {
            player.setDataSource(getApplicationContext(), trackUri);

        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);

        }

        player.prepareAsync();


    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
        prepared = true;

        //start playback


//        Notification.Builder builder = new Notification.Builder(this);

        //      builder.setContentIntent(pendInt)
        //            .setSmallIcon(R.drawable.play)
        //          .setTicker(songTitle)
        //        .setOngoing(true)
        //      .setContentTitle("Playing")
        // .setContentText(songTitle);
        //     Notification not = builder.build();

        //   startForeground(NOTIFY_ID, not);


    }


    public void setSong(int songIndex) {
        songPosn = songIndex;
    }


    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {

        return songs.get(songPosn).getDuration();
    }


    public String getSongTitle() {
        return songs.get(songPosn).getTitle();
    }

    public String getSongArtist() {
        return songs.get(songPosn).getArtist();
    }

    public String getSongAlbum() {
        return songs.get(songPosn).getAlbum();
    }

    public String getSongAlbumArtId() {
        return songs.get(songPosn).getAlbumArtID();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        player.start();
    }

    public void playPrev() {
        songPosn--;
        if (songPosn >= songs.size()) {
            songPosn = 0;
        }
        if (songPosn < 0) {
            songPosn = songs.size() - 1;
        }

        playSong();
    }

    //skip to next
    public void playNext() {

        songPosn++;
        if (songPosn >= songs.size()) {
            songPosn = 0;
        }
        if (songPosn < 0) {
            songPosn = songs.size() - 1;
        }

        playSong();

    }

    public ArrayList<Song> getQueue() {

        ArrayList<Song> queue = new ArrayList<Song>();

        for (int i = songPosn + 1; i < songs.size(); i++) {

            queue.add(songs.get(i));


        }


        return queue;
    }


    @Override
    public void onDestroy() {
        stopForeground(true);
    }


    public void setShuffle() {


        if (shuffle == false) {
            Collections.shuffle(songs);
            shuffle = true;
            songPosn = 0;
        } else {


            shuffle = false;
            Song temp = songs.get(songPosn);
            int count = 0;
            Collections.sort(songs);

            for (Song sng : songs) {
                if (sng.getID() == temp.getID()) {
                    songPosn = count;
                }
                count++;
            }

        }


    }


    public void setLoop() {

        if (player.isLooping() == true) {
            player.setLooping(false);

        } else {
            player.setLooping(true);

        }

    }

    public void clearPrep() {
        prepared = false;
    }

    public boolean getPrep() {

        return prepared;
    }

    public void prepare2() {
        try {
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaPlayer getPlayer() {

        return player;
    }

    public void releaser() {
        player.release();
    }


    public boolean getShufle() {
        return shuffle;
    }

}
