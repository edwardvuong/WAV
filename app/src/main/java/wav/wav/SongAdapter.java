package wav.wav;

import android.graphics.drawable.Drawable;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.view.View;
import android.view.ViewGroup;


public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private ArrayList<Song> searchList;
    private LayoutInflater songInf;


    ArrayList<String> albumList = new ArrayList<>();
    ArrayList<String> artistList = new ArrayList<>();


    private String section;

    private boolean search;


//    public SongAdapter(Context c, ArrayList<Song> theSongs){
//        songs = theSongs;
//        songInf = LayoutInflater.from(c);
//        section = "song";
//    }

    public SongAdapter(Context c, ArrayList<Song> theSongs, String newSection) {
        songs = theSongs;

        searchList = songs;

        search = false;

        songInf = LayoutInflater.from(c);
        section = newSection;


        for (Song s : songs) {
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

            if (search == true) {


                return searchList.size();
            } else {
                return songs.size();
            }

        } else if (section == "album") {
            return albumList.size() / 2;
        } else {
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

    public void setSection(String newSection) {

        section = newSection;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (section == "song" || search == true) {


            //map to song layout
            RelativeLayout songLay = (RelativeLayout) songInf.inflate
                    (R.layout.song, parent, false);


            //get title and artist views
            TextView songView = (TextView) songLay.findViewById(R.id.song_title);
            TextView artistView = (TextView) songLay.findViewById(R.id.song_artist);
            //get song using position

            Song currSong;

            if (search == true) {
                currSong = searchList.get(position);
            } else {
                currSong = songs.get(position);
            }

            //get title and artist strings
            songView.setText(currSong.getTitle());
            artistView.setText(currSong.getArtist());
            //set position as tag
            songLay.setTag(position);
            return songLay;
        } else if (section == "album") {


            RelativeLayout songLay = (RelativeLayout) songInf.inflate
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


            RelativeLayout songLay = (RelativeLayout) songInf.inflate
                    (R.layout.artist, parent, false);
            //get title and artist views
            TextView songArtist = (TextView) songLay.findViewById(R.id.song_artist);
            TextView trackNums = (TextView) songLay.findViewById(R.id.track_num);
            //get song using position
            String currSong = artistList.get(position);
            //get title and artist strings
            songArtist.setText(currSong);
            int count = 0;
            for (Song sng : songs) {
                if (sng.getArtist().equals(currSong)) {
                    count++;
                }

            }

            if (count != 1) {
                trackNums.setText(count + " Tracks");
            } else {
                trackNums.setText(count + " Track");
            }

            //set position as tag
            songLay.setTag(position);
            return songLay;
        }


    }


    public void search(String searchVal) {

        searchList = songs;

        search = true;

        ArrayList<Song> searchListTemp = new ArrayList<Song>();

        if (section == "album") {

            for (Song sng : searchList) {

                if (sng.getAlbum().equals(searchVal)) {
                    searchListTemp.add(sng);
                }

            }

        } else if (section == "artist") {

            for (Song sng : searchList) {

                if (sng.getArtist().equals(searchVal)) {
                    searchListTemp.add(sng);
                }

            }


        } else {


            for (Song sng : searchList) {

                if (sng.getArtist().toLowerCase().contains(searchVal.toLowerCase()) || sng.getAlbum().toLowerCase().contains(searchVal.toLowerCase()) || sng.getTitle().toLowerCase().contains(searchVal.toLowerCase())) {
                    searchListTemp.add(sng);
                }

            }


        }


        searchList = searchListTemp;


    }


    public ArrayList<Song> retList() {

        if (search == true) {
            return searchList;
        } else {
            return songs;
        }

    }

    public void clearSearch() {

        search = false;

    }

}
