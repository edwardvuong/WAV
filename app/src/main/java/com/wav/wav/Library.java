package com.wav.wav;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Library extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        // Now Playing Button
        Button np = (Button)findViewById(R.id.nowPlayingBtn);

        np.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Library.this, MainActivity.class));
                //finish();
            }
        });

        // Songs Button
        Button songs = (Button)findViewById(R.id.songs);

        songs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Library.this, SongList.class));
                //finish();
            }
        });
    }
}
