package wav.wav;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.d.lib.tabview.TabView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;

import wav.wav.MusicService.MusicBinder;


//PLAYER

import android.graphics.drawable.Drawable;
import android.media.AudioManager;

import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import android.widget.SeekBar;

public class Wav extends AppCompatActivity {
    private static final String TAG = "DemoActivity";

    private SlidingUpPanelLayout mLayout;

    /**
     * LIBRARY
     */

    protected ArrayList<Song> songList;
    protected ListView songView;
    protected MusicService musicSrv;
    protected Intent playIntent;
    protected boolean musicBound = false;

    protected boolean paused = false, playbackPaused = false;
    TextView title;
    SongAdapter songAdt;
    SongAdapter queueAdt;
    protected ListView queueView;
    SearchView srch;

    /**
     * Menu
     */

    private TabView[] tabViews = new TabView[3];

    /**
     * PLAYER
     *
     * @param savedInstanceState
     */
    TextView songTitle;
    TextView songAlbum;
    TextView songArtist;
    ImageView albumArt;

    Button playBtn;
    Button loopBtn;
    SeekBar positionBar;
    SeekBar volumeBar;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;

    int totalTime;
    Button nextBtn;
    Button prevBtn;
    Button shuffleBtn;


    ArrayList<Song> queue;
    private AudioManager audioManager;
    private boolean canChange = true;
    private boolean wasPlaying = false;
    private boolean justShuff = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wav_activity);

        srch = (SearchView) findViewById(R.id.searchBar);

        ListView lv = (ListView) findViewById(R.id.list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(Wav.this, "onItemClick", Toast.LENGTH_SHORT).show();
            }
        });

        List<String> your_array_list = Arrays.asList(
                "This",
                "Of",
                "SlidingUpPanelLayout"
        );

        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                your_array_list);

        lv.setAdapter(arrayAdapter);

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });
        mLayout.setFadeOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(PanelState.COLLAPSED);
            }
        });

        /**
         * LIBRARY
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return;
            }
        }


        songView = (ListView) findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        getSongList();

        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        songAdt = new SongAdapter(this, songList, "song");
        songView.setAdapter(songAdt);

        /**
         * MENU
         */

        tabViews[2] = (TabView) findViewById(R.id.tabv_tab2);

        for (int i = 2; i < tabViews.length; i++) {
            final int finalI = i;
            tabViews[finalI].setOnTabSelectedListener(new TabView.OnTabSelectedListener() {
                @Override
                public void onTabSelected(int index) {

                    if (index == 0) {
                        songAdt.clearSearch();
                        songAdt.setSection("song");
                        songView.setAdapter(songAdt);
                    }
                    if (index == 1) {
                        songAdt.clearSearch();
                        songAdt.setSection("album");
                        songView.setAdapter(songAdt);
                    }
                    if (index == 2) {
                        songAdt.clearSearch();
                        songAdt.setSection("artist");
                        songView.setAdapter(songAdt);
                    }
                    if (index == 3) {
                        songAdt.clearSearch();
                        songAdt.setSection("song");
                        songView.setAdapter(songAdt);
                    }
                }
            });
        }

        /**
         *  PLAYER
         */

        playBtn = (Button) findViewById(R.id.playBtn);
        // loopBtn= (Button) findViewById(R.id.loopBtn);
        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);

        nextBtn = (Button) findViewById(R.id.nextBtn);
        prevBtn = (Button) findViewById(R.id.prevBtn);

        shuffleBtn = (Button) findViewById(R.id.shuffleBtn);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.demo, menu);
        MenuItem item = menu.findItem(R.id.action_toggle);
        if (mLayout != null) {
            if (mLayout.getPanelState() == PanelState.HIDDEN) {
                item.setTitle(R.string.action_show);
            } else {
                item.setTitle(R.string.action_hide);
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggle: {
                if (mLayout != null) {
                    if (mLayout.getPanelState() != PanelState.HIDDEN) {
                        mLayout.setPanelState(PanelState.HIDDEN);
                        item.setTitle(R.string.action_show);
                    } else {
                        mLayout.setPanelState(PanelState.COLLAPSED);
                        item.setTitle(R.string.action_hide);
                    }
                }
                return true;
            }
            case R.id.action_anchor: {
                if (mLayout != null) {
                    if (mLayout.getAnchorPoint() == 1.0f) {
                        mLayout.setAnchorPoint(0.7f);
                        mLayout.setPanelState(PanelState.ANCHORED);
                        item.setTitle(R.string.action_anchor_disable);
                    } else {
                        mLayout.setAnchorPoint(1.0f);
                        mLayout.setPanelState(PanelState.COLLAPSED);
                        item.setTitle(R.string.action_anchor_enable);
                    }
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == PanelState.EXPANDED || mLayout.getPanelState() == PanelState.ANCHORED)) {
            mLayout.setPanelState(PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * LIBRARY
     */
    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, sortOrder);

        if (musicCursor != null && musicCursor.moveToFirst()) {
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
            musicCursor.close();
            ;
        }

    }


    private static String getCoverArtPath(ContentResolver musicResolver, long androidAlbumId) {
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

    public void songPicked(View view) {


        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));

        if (view.getParent() == queueView) {

            musicSrv.setList(queueAdt.retList());
        } else {
            musicSrv.setList(songAdt.retList());
        }
        musicSrv.playSong();


        if (mLayout != null) {
            if (mLayout.getAnchorPoint() == 1.0f) {
                mLayout.setAnchorPoint(0.0f);
                mLayout.setPanelState(PanelState.ANCHORED);
            } else {
                mLayout.setAnchorPoint(1.0f);
                mLayout.setPanelState(PanelState.COLLAPSED);
            }
        }


        updateInfo();

    }


    public void albumPicked(View view) {
        TextView t = view.findViewById(R.id.song_album);
        songAdt.search(t.getText().toString());
        songAdt.setSection("song");
        songView.setAdapter(songAdt);
    }

    public void artistPicked(View view) {
        TextView t = view.findViewById(R.id.song_artist);
        songAdt.search(t.getText().toString());
        songAdt.setSection("song");
        songView.setAdapter(songAdt);

    }

    public void inputSearch(View view) {

        srch.setActivated(true);

        srch.onActionViewExpanded();
        srch.setIconified(false);
        srch.clearFocus();
        srch.setQueryHint("Search Library: ");
        srch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                songAdt.clearSearch();
                songAdt.setSection("song");
                songView.setAdapter(songAdt);

                songAdt.search(query);
                songView.setAdapter(songAdt);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }

        });
    }

    /**
     * PLAYER
     */
    protected ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
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


    public void updateInfo() {


        if (justShuff == false) {

            totalTime = musicSrv.getDur();

            songTitle = (TextView) findViewById(R.id.sTitle);
            songAlbum = (TextView) findViewById(R.id.sAlbum);
            songArtist = (TextView) findViewById(R.id.sArtist);
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
            TextView a = (TextView) findViewById(R.id.barSong);
            a.setText(musicSrv.getSongTitle());


            TextView t = (TextView) findViewById(R.id.barArtist);
            t.setText(musicSrv.getSongArtist());

            if (musicSrv.isPng()) {
                playBtn.setBackgroundResource(R.drawable.play);

            } else {
                playBtn.setBackgroundResource(R.drawable.pause);

            }

        }


        justShuff = false;

        albumArt.setOnClickListener(new OnClickListener() {

            @Override

            public void onClick(View v) {
                musicSrv.setLoop();

            }
        });





        // Position Bar
        positionBar = (SeekBar) findViewById(R.id.positionBar);
        positionBar.setMax(totalTime);
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
                            canChange = false;

                            musicSrv.pausePlayer();


                        }
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

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        volumeBar.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        volumeBar.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                progress, 0);

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
                        Message mst = new Message();

                        Thread.sleep(1000);

                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();


        queueView = (ListView) findViewById(R.id.list);


        queue = musicSrv.getQueue();
        queueAdt = new SongAdapter(this, queue, "song");


        queueView.setAdapter(queueAdt);


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
//
//            if (totalTime - currentPosition <= 0 && canChange == true) {
//                musicSrv.playNext();
//                updateInfo2();
//            }

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

    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }


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

    public void shuffleBtnClick(View view) {

        Button btn = findViewById(R.id.shuffleBtn);

        musicSrv.setShuffle();
        if (musicSrv.getShufle() == true) {
            btn.setBackgroundResource(R.drawable.shuffleclick);
        } else {
            btn.setBackgroundResource(R.drawable.shuffle);
        }
        justShuff = true;
        updateInfo();


    }

}