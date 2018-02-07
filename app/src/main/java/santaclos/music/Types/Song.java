package santaclos.music.Types;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

public class Song extends Object implements Parcelable
{
    private boolean downloaded;
    private String artist;
    private String album;
    private int number;
    private String title;
    private String mp3Url;
    private String lyricsUrl;

    public Song(String artist, String album, String title, String mp3Url, String lyricsUrl, int number, SharedPreferences prefs)
    {
        this.artist=artist;
        this.album=album;
        this.number=number;
        this.title=title;
        this.mp3Url=mp3Url;
        this.lyricsUrl=lyricsUrl;

        if(prefs!=null)
            downloaded=HasBeenDownloaded(prefs);
    }

    public String getTitle() {return title;}
    public String getAlbum() {return album;}
    public String getArtist() {return artist;}
    public String getMp3Url() {return mp3Url;}
    public int getNumber(){return number;}
    public boolean getDownlaoded(){return downloaded;}

    public String getFilePath()
    {
        return RemoveWeirdChars(artist)+"/"+RemoveWeirdChars(album)+"/"+ (number>9?String.valueOf(number):("0" + String.valueOf(number))) + "_" + RemoveWeirdChars(title)+".mp3";
    }

    static String RemoveWeirdChars(String s)
    {
        final String available = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final char space = ' ';
        String res = "";

        for (int i = 0; i<s.length(); i++)
        {
            if (available.contains(String.valueOf(s.charAt(i))))
                res += s.charAt(i);
            else if (s.charAt(i) == space)
                res += '_';
        }
        return res;
    }
    boolean HasBeenDownloaded(SharedPreferences prefs)
    {
        return prefs.getBoolean(getFilePath(), false);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(downloaded?1:0);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeInt(number);
        dest.writeString(title);
        dest.writeString(mp3Url);
        dest.writeString(lyricsUrl);
    }
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        public Song createFromParcel(Parcel pc) {
            return new Song(pc);
        }
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
    public Song(Parcel pc){
        downloaded = pc.readInt()>0;
        artist = pc.readString();
        album = pc.readString();
        number = pc.readInt();
        title = pc.readString();
        mp3Url = pc.readString();
        lyricsUrl = pc.readString();
    }
}
