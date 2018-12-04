package wav.wav;

import android.graphics.drawable.Drawable;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.view.View;
import 	android.view.ViewGroup;


public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInf;


    ArrayList<String> albumList = new ArrayList<>();
    ArrayList<String> artistList = new ArrayList<>();
    int count;


    private String section;


//    public SongAdapter(Context c, ArrayList<Song> theSongs){
//        songs = theSongs;
//        songInf = LayoutInflater.from(c);
//        section = "song";
//    }

    public SongAdapter(Context c, ArrayList<Song> theSongs, String newSection){
        songs=theSongs;
        songInf=LayoutInflater.from(c);
        section = newSection;


        for( Song s : songs){
            if (!albumList.contains(s.getAlbum())) {
                albumList.add(s.getAlbum());
                albumList.add(s.getAlbumArtID());
            }

            if (!artistList.contains(s.getArtist())) {
                artistList.add(s.getArtist());
            }
        }

    }

    @Override
    public int getCount() {
        if (section == "song") {

            return songs.size();
        }
        else if (section =="album"){
            count = 0;
            return albumList.size()/2;
        }
        else{
            return artistList.size();
        }


    }
    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setSection(String newSection){

        section = newSection;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (section == "song") {


            System.out.println("SONGPOS: " + position);

            //map to song layout
            LinearLayout songLay = (LinearLayout) songInf.inflate
                    (R.layout.song, parent, false);
            //get title and artist views
            TextView songView = (TextView) songLay.findViewById(R.id.song_title);
            TextView artistView = (TextView) songLay.findViewById(R.id.song_artist);
            //get song using position
            Song currSong = songs.get(position);
            //get title and artist strings
            songView.setText(currSong.getTitle());
            artistView.setText(currSong.getArtist());
            //set position as tag
            songLay.setTag(position);
            return songLay;
        } else if (section == "album") {


            System.out.println("ALBUMPOS: " + position);


            LinearLayout songLay = (LinearLayout) songInf.inflate
                    (R.layout.album, parent, false);
            //get title and artist views


            TextView songAlbum = (TextView) songLay.findViewById(R.id.song_album);
            ImageView albumArt = (ImageView) songLay.findViewById(R.id.album_art);
            //get song using position
            String currAlbum = albumList.get(position * 2);
            String currAlbumArt = albumList.get(position * 2 + 1);


            //get title and artist strings
            songAlbum.setText(currAlbum);
            albumArt.setImageDrawable(Drawable.createFromPath(currAlbumArt));

            //set position as tag
            songLay.setTag(position);
            return songLay;


        } else {

            System.out.println("ARTISTPOS: " + position);


            LinearLayout songLay = (LinearLayout) songInf.inflate
                    (R.layout.artist, parent, false);
            //get title and artist views
            TextView songArtist = (TextView) songLay.findViewById(R.id.song_artist);
            TextView trackNums = (TextView) songLay.findViewById(R.id.track_num);
            //get song using position
            String currSong = artistList.get(position);
            //get title and artist strings
            songArtist.setText(currSong);
            trackNums.setText("-1");
            //set position as tag
            songLay.setTag(position);
            return songLay;
        }


    }
}