package santaclos.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

import santaclos.music.Adapters.AlbumAdapter;
import santaclos.music.Adapters.ArtistAdapter;
import santaclos.music.Adapters.PlaylistAdapter;
import santaclos.music.Adapters.SongAdapter;
import santaclos.music.Types.Album;
import santaclos.music.Types.Artist;
import santaclos.music.Types.Song;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private ArtistAdapter artistAdapter;
    private AlbumAdapter albumAdapter;
    private SongAdapter songAdapter;

    private PlaylistAdapter playlistAdapter;

    private List<Artist> artists = new ArrayList<>();
    private List<Album> albums = new ArrayList<>();
    private ArrayList<Song> songs = new ArrayList<>();

    private List<Song> playlist = new ArrayList<>();
    public int playlistPlayingIndex;

    int currentAdapter = 0;//artist,album,song         artist\playlist,album\playlist,song\playlist

    private String artistsURL = "https://raw.githubusercontent.com/shiabehugo/48otw/master/artists.dat";
    private String albumsURL;
    private String songsURL;

    Button backButton;
    RelativeLayout menuButton;

    Button prevButton;
    Button pauseButton;
    Button nextButton;

    SharedPreferences prefs;
    SharedPreferences.Editor prefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("app", MODE_PRIVATE);
        prefsEditor = getSharedPreferences("app", MODE_PRIVATE).edit();


        Typeface t = Typeface.createFromAsset(getAssets(), "music.ttf");

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        backButton = (Button) findViewById(R.id.back_toolbar_button);
        menuButton = (RelativeLayout) findViewById(R.id.menu_toolbar_button);
        prevButton = (Button) findViewById(R.id.prev_control_button);
        pauseButton = (Button) findViewById(R.id.pause_control_button);
        nextButton = (Button) findViewById(R.id.next_control_button);

        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        artistAdapter = new ArtistAdapter(artists, this);
        albumAdapter = new AlbumAdapter(albums, this, prefsEditor, prefs);
        songAdapter = new SongAdapter(songs, this, prefsEditor, t);

        playlistAdapter = new PlaylistAdapter(playlist, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        backButton.setTypeface(t);
        prevButton.setTypeface(t);
        pauseButton.setTypeface(t);
        nextButton.setTypeface(t);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoBack();
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(v.getContext(), MusicService.class);

                service.putExtra("cmd", MusicService.CMD_PREVIOUS);
                startService(service);
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(v.getContext(), MusicService.class);

                service.putExtra("cmd", MusicService.CMD_PAUSE);
                startService(service);
                //TogglePauseButton();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(v.getContext(), MusicService.class);

                service.putExtra("cmd", MusicService.CMD_NEXT);
                startService(service);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder aD = new AlertDialog.Builder(menuButton.getContext());

                String[] buttons = MusicService.isRunning?new String[]{"Playlist", "Exit"}:new String[]{"Playlist"};

                aD.setItems(buttons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which)
                        {
                            case 0:
                            {
                                LoadPlaylist();
                                break;
                            }
                            case 1:
                            {
                                Intent service = new Intent(getBaseContext(), MusicService.class);

                                service.putExtra("cmd", MusicService.CMD_EXIT);
                                startService(service);
                                playlist.clear();
                                playlistAdapter.notifyDataSetChanged();
                                HideControlButtons();
                            }
                        }
                    }
                });
                aD.show();
            }
        });

        LoadArtists();


        if(MusicService.isRunning) {
            ShowControlButtons();

            Intent service = new Intent(this, MusicService.class);
            service.putExtra("cmd", MusicService.CMD_REQUEST_IS_PLAYING);
            startService(service);
            service.putExtra("cmd", MusicService.CMD_REQUEST_PLAYLIST);
            startService(service);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("broadcast-event"));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getIntExtra("cmd", 0))
            {
                case MusicService.BROADCAST_CMD_SEND_PLAYLIST:
                {
                    playlist.clear();
                    ArrayList<Song> received = intent.getParcelableArrayListExtra("playlist");
                    for (Song s : received)
                        playlist.add(s);
                    playlistPlayingIndex = intent.getIntExtra("playingIndex", 0);
                    break;
                }
                case MusicService.BROADCAST_CMD_SEND_PLAYLIST_AND_RELOAD:
                {
                    playlist.clear();
                    ArrayList<Song> received = intent.getParcelableArrayListExtra("playlist");
                    for (Song s : received)
                        playlist.add(s);
                    playlistPlayingIndex = intent.getIntExtra("playingIndex", 0);
                    playlistAdapter.notifyDataSetChanged();
                    break;
                }
                case MusicService.BROADCAST_CMD_SEND_IS_PLAYING:
                    if(intent.getBooleanExtra("isPlaying", false))
                        SetPauseButton();
                    else
                        SetPlayButton();
                    break;
                case MusicService.BROADCAST_CMD_NOTIFY_PLAY_NEXT:
                    if(currentAdapter > 2) {
                        playlistPlayingIndex++;
                        playlistAdapter.notifyItemRangeChanged(intent.getIntExtra("toPlay", 1)-1, 2);
                    }
                    break;
                case MusicService.BROADCAST_CMD_NOTIFY_PLAY_PREV:
                    if(currentAdapter > 2) {
                        playlistPlayingIndex--;
                        playlistAdapter.notifyItemRangeChanged(intent.getIntExtra("toPlay", 1), 2);
                    }
                    break;
            }
        }
    };

    //navigation

    public void LoadArtists() {


        currentAdapter = 0;
        artists.clear();
        recyclerView.setAdapter(artistAdapter);
        backButton.setVisibility(View.INVISIBLE);

        String data = prefs.getString(artistsURL, null);

        if(data == null) {

            Ion.with(getApplicationContext())
                    .load(artistsURL)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {

                            if(e != null)
                            {
                                Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                            }

                            prefsEditor.putString(artistsURL, result);
                            prefsEditor.commit();

                            LoadArtistsFromString(result);
                        }
                    });
        }
        else {

            LoadArtistsFromString(data);
        }



        /*currentAdapter = 0;
        artists.clear();
        recyclerView.setAdapter(artistAdapter);

        Ion.with(getApplicationContext())
                .load(artistsURL)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        String lineSplit[] = result.split("\\n");
                        for (int i = 0; i < lineSplit.length; i++) {
                            //Toast.makeText(getApplicationContext(), "asdfas", Toast.LENGTH_SHORT).show();
                            String split[] = lineSplit[i].split("\\\\");
                            artists.add(new Artist(split[0], split[1]));
                        }
                        artistAdapter.notifyDataSetChanged();
                    }
                });
        backButton.setVisibility(View.INVISIBLE);*/
    }

    void LoadArtistsFromString(String data)
    {
        String lineSplit[] = data.split("\\n");
        for (String aLineSplit : lineSplit) {
            String split[] = aLineSplit.split("\\\\");
            artists.add(new Artist(split[0], split[1]));
        }
        artistAdapter.notifyDataSetChanged();
    }

    public void LoadAlbums(final String url) {

        currentAdapter = 1;
        albumsURL = url;
        albums.clear();
        recyclerView.setAdapter(albumAdapter);
        backButton.setVisibility(View.VISIBLE);


        String data = prefs.getString(url, null);

        if(data == null) {

            Ion.with(getApplicationContext())
                    .load(url)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {

                            if(e != null)
                            {
                                Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                            }

                            prefsEditor.putString(url, result);
                            prefsEditor.commit();

                            LoadAlbumsFromString(result);
                        }
                    });
        }
        else
        {
            LoadAlbumsFromString(data);
        }
    }

    public void LoadAlbums() {
        LoadAlbums(albumsURL);
    }

    void LoadAlbumsFromString(String data)
    {
        String lineSplit[] = data.split("\\n");
        for (int i = 0; i < lineSplit.length; i++) {
            String split[] = lineSplit[i].split("\\\\");
            albums.add(new Album(split[0], split[1], split[2]));
        }
        albumAdapter.notifyDataSetChanged();
    }

    public void LoadSongs(final String url) {
        currentAdapter = 2;
        songsURL = url;
        songs.clear();
        recyclerView.setAdapter(songAdapter);



        String data = prefs.getString(url, null);

        if(data == null) {

            Ion.with(getApplicationContext())
                    .load(url)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {

                            if(e != null)
                            {
                                Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                            }

                            prefsEditor.putString(url, result);
                            prefsEditor.commit();

                            LoadSongsFromString(result);
                        }
                    });
        }
        else
        {
            LoadSongsFromString(data);
        }
    }

    public void LoadSongs() {
        LoadSongs(songsURL);
    }

    void LoadSongsFromString(String data)
    {
        String lineSplit[] = data.split("\\n");
        String[] artistAlbum = lineSplit[0].split("\\\\");
        for (int i = 1; i < lineSplit.length; i++) {
            String split[] = lineSplit[i].split("\\\\");
            songs.add(new Song(artistAlbum[0], artistAlbum[1], split[0], split[1], split.length > 2 ? split[2] : "", i, prefs));
        }
        songAdapter.notifyDataSetChanged();
    }

    private void LoadPlaylist() {
        if(currentAdapter<3)
        {
            currentAdapter += 3;
            recyclerView.setAdapter(playlistAdapter);
            playlistAdapter.notifyDataSetChanged();
            backButton.setVisibility(View.VISIBLE);
        }
    }

    private void GoBack() {
        currentAdapter--;
        if(currentAdapter > 1) {
            currentAdapter-=2;
            switch (currentAdapter){
                case 0:
                    LoadArtists();
                    backButton.setVisibility(View.INVISIBLE);
                    break;
                case 1: LoadAlbums();break;
                case 2: LoadSongs();
            }
        } else if (currentAdapter > 0) {
            LoadAlbums();
        } else if (currentAdapter > -1) {
            LoadArtists();
        } else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            currentAdapter = 0;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            GoBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //navigation

    //UI

    public void SetPauseButton(){pauseButton.setText("P");}
    public void SetPlayButton(){pauseButton.setText("p");}
    public void ShowControlButtons(){
        prevButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }
    public void HideControlButtons(){
        prevButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
    }
    public void TogglePauseButton(){
        if(pauseButton.getText().toString().matches("p"))
            pauseButton.setText("P");
        else
            pauseButton.setText("p");
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
