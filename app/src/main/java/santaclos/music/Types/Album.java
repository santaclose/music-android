package santaclos.music.Types;

public class Album
{
    String title;
    String songListURL;
    String artworkURL;

    public Album(String title, String songListURL, String artworkURL)
    {
        this.title=title;
        this.songListURL=songListURL;
        this.artworkURL=artworkURL;
    }
    public String getTitle(){return title;}
    public String getSongListURL(){return songListURL;}
    public String getArtworkURL(){return artworkURL;}
}
