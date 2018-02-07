package santaclos.music.Types;

public class Artist {
    String name;
    String albumsURL;
    public Artist(String name, String albumsURL)
    {
        this.name=name;
        this.albumsURL=albumsURL;
    }
    public String getName(){return name;}
    public String getAlbumsURL(){return albumsURL;}
}
