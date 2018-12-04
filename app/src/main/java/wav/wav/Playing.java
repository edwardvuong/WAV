package wav.wav;


import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.Manifest;

import android.os.Handler;
import android.os.Message;
import android.os.TestLooperManager;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;


import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import wav.wav.MusicService.MusicBinder;


public class Playing extends AppCompatActivity {

    protected ArrayList<Song> songList;
    protected ListView songView;
    protected MusicService musicSrv;
    protected Intent playIntent;
    protected boolean musicBound = false;

    TextView songTitle;
    TextView songAlbum;
    TextView songArtist;
    ImageView albumArt;
    Button libBtn;

    protected boolean paused = false, playbackPaused = false;


    Button playBtn;
    Button loopBtn;
    SeekBar positionBar;
    SeekBar volumeBar;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;


    AnimationDrawable ptp; //PlayToPause animation
    int totalTime;


    Button nextBtn;
    Button prevBtn;

    private boolean canChange = true;

    private boolean wasPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        songList = getIntent().getParcelableArrayListExtra("songList");


        playBtn = (Button) findViewById(R.id.playBtn);
        // loopBtn= (Button) findViewById(R.id.loopBtn);
        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);

        nextBtn = (Button) findViewById(R.id.nextBtn);
        prevBtn = (Button) findViewById(R.id.prevBtn);

    }


    //connect to the service
    protected ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;


            playSong();

            updateInfo();


        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    public void updateInfo() {

        //mp.setVolume(0.5f, 0.5f);
        totalTime = musicSrv.getDur();

        songTitle = (TextView) findViewById(R.id.song_title);
        songAlbum = (TextView) findViewById(R.id.song_album);
        songArtist = (TextView) findViewById(R.id.song_artist);

        albumArt = (ImageView) findViewById(R.id.imageView3);


        songTitle.setText(musicSrv.getSongTitle());

        songAlbum.setText(musicSrv.getSongAlbum());
        songArtist.setText(musicSrv.getSongArtist() + " ― ");

        albumArt.setImageDrawable(Drawable.createFromPath(musicSrv.getSongAlbumArtId()));
        albumArt.setMinimumWidth(500);
        albumArt.setMaxWidth(500);
        albumArt.setMinimumHeight(500);
        albumArt.setMaxHeight(500);
        albumArt.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Position Bar
        positionBar = (SeekBar) findViewById(R.id.positionBar);
        positionBar.setMax(totalTime);
        System.out.println("MAX: " + positionBar.getMax());
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            musicSrv.seek(progress);
                            positionBar.setProgress(progress);

                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                        if (musicSrv.isPng()) {
                            wasPlaying = true;
                        } else {
                            wasPlaying = false;
                        }

                        canChange = false;
                        musicSrv.pausePlayer();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        canChange = true;

                        if (wasPlaying) {
                            musicSrv.go();
                        }

                    }
                }
        );


        // Volume Bar
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volumeNum = progress / 100f;
                        //mp.setVolume(volumeNum, volumeNum);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );


        // Thread (Update positionBar & timeLabel)
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (musicSrv != null) {
                    try {
                        Message msg = new Message();
                        msg.what = musicSrv.getPosn();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();

        songView = (ListView)findViewById(R.id.song_list);

        ArrayList<Song> queue = musicSrv.getQueue();

        SongAdapter songAdt = new SongAdapter(this, queue, "song");
        songView.setAdapter(songAdt);

    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update positionBar.
            positionBar.setProgress(currentPosition);

            // Update Labels.
            String elapsedTime = createTimeLabel(currentPosition);
            elapsedTimeLabel.setText(elapsedTime);

            String remainingTime = createTimeLabel(totalTime - currentPosition);
            remainingTimeLabel.setText("- " + remainingTime);

            if (totalTime - currentPosition <= 0 && canChange == true) {
                musicSrv.playNext();
                updateInfo();
            }

        }
    };

    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

    }


    public void playSong() {
        String s = getIntent().getStringExtra("SetSong");
        musicSrv.setSong(Integer.parseInt(s));
        musicSrv.playSong();

        playBtn.setBackgroundResource(R.drawable.pause);

        System.out.println("DURATION: " + musicSrv.getDur());


    }


    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }


    //  @Override
    // public boolean onOptionsItemSelected(MenuItem item) {
    //   switch (item.getItemId()) {
    //     case R.id.loopBtn:
    //       musicSrv.setShuffle();
    //     break;
    // }
    // return false;
    // }


    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }


    public void playBtnClick(View view) {
        if (!musicSrv.isPng()) {
            // Stopping
            playBtn.setBackgroundResource(R.drawable.pause);
            musicSrv.go();
        } else {
            // Playing
            musicSrv.pausePlayer();
            playBtn.setBackgroundResource(R.drawable.play);

        }

    }

    public void nextBtnClick(View view) {

        musicSrv.playNext();
        updateInfo();

    }

    public void prevBtnClick(View view) {
        musicSrv.playPrev();
        updateInfo();
    }


    public void toLib(View view) {
        startActivity(new Intent(Playing.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    public void songPicked(View view){
        startActivity(new Intent(Playing.this, Playing.class).putExtra("SetSong", Integer.toString(Integer.parseInt(view.getTag().toString()))).putExtra("songList", songList));

    }

}