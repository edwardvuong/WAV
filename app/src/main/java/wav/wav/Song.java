package wav.wav;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable{

    private long id;
    private String title;
    private String artist;

    private  int duration;


    public Song(long songID, String songTitle, String songArtist, int songDuration) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        duration = songDuration;
    }


    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        duration = in.readInt();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public long getID(){return id;} //CAN ADD MORE VARIABLES
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public  int getDuration() {return duration;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeInt(duration);
    }
}
