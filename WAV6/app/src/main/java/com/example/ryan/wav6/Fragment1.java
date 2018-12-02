package com.example.ryan.wav6;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class Fragment1 extends Fragment
{
    private static final String TAG = "Fragment1";

    private Button btnNavPlayer;
    private Button btnNavLibrary;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState)
    {
        View view  = inflater.inflate(R.layout.home_layout, container, false);
        btnNavPlayer = (Button) view.findViewById(R.id.btnNavPlayer);
        btnNavLibrary = (Button) view.findViewById(R.id.btnNavLibrary);

        Log.d(TAG, "onCreateView: started.");

        btnNavPlayer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(getActivity(), "Going to Player", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), Player.class);
                startActivity(intent);
            }
        });

        btnNavLibrary.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(getActivity(), "Going to Library", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LibraryActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}