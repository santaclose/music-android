package santaclos.music;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import santaclos.music.Types.Song;

public class MusicService extends Service {

    public static boolean isRunning = false;
    public static final int FOREGROUND_ID = 1243;
    public static String PREV_ACTION = "santaclos.MusicService.action.prev";
    public static String PAUSE_ACTION = "santaclos.MusicService.action.play";
    public static String NEXT_ACTION = "santaclos.MusicService.action.next";

    public static final int CMD_PLAY_FROM_SONG_ADAPTER = 0;
    public static final int CMD_PREVIOUS = 1;
    public static final int CMD_PAUSE = 2;
    public static final int CMD_NEXT = 3;
    public static final int CMD_ADD_NEXT = 4;
    public static final int CMD_ADD_TO_QUEUE = 5;
    public static final int CMD_REMOVE_FROM_PLAYLIST = 6;
    public static final int CMD_REQUEST_IS_PLAYING = 7;
    public static final int CMD_REQUEST_PLAYLIST = 8;
    public static final int CMD_PLAY_PLAYLIST_AT_INDEX = 9;
    public static final int CMD_EXIT = 10;

    private MediaPlayer mp = null;
    private ArrayList<Song> playlist = new ArrayList<>();
    private ArrayList<Song> queue = new ArrayList<>();
    private int currentPlaying;
    //private int queuePosition;

    private Notification buildForegroundNotification() {

        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent popenAppIntent = PendingIntent.getActivity(this, 0,
                openAppIntent, 0);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction(PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(PAUSE_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_play_arrow_black_24dp);

        RemoteViews mContentView = new RemoteViews(getPackageName(), R.layout.notification);
        mContentView.setOnClickPendingIntent(R.id.not_open_app, popenAppIntent);
        mContentView.setOnClickPendingIntent(R.id.not_prev, ppreviousIntent);
        mContentView.setOnClickPendingIntent(R.id.not_pause, pplayIntent);
        mContentView.setOnClickPendingIntent(R.id.not_next, pnextIntent);

        mContentView.setTextViewText(R.id.title, "Music");
        mBuilder.setContent(mContentView);

        return mBuilder.build();
    }

    public IBinder onBind(Intent arg0) {

        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        isRunning=true;
        startForeground(FOREGROUND_ID, buildForegroundNotification());
        //Toast.makeText(this, "create",Toast.LENGTH_SHORT).show();
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getIntExtra("cmd", -1)){
            case CMD_PLAY_FROM_SONG_ADAPTER:
                //Toast.makeText(this, "playFromSongAdapter",Toast.LENGTH_SHORT).show();
                ArrayList<Song> newPlaylist = intent.getParcelableArrayListExtra("songList");
                int newPos = intent.getIntExtra("position", 0);
                PlayFromSongAdapter(newPlaylist, newPos);
                break;
            case CMD_PREVIOUS:
                //Toast.makeText(this, "playPrevious",Toast.LENGTH_SHORT).show();
                PlayPrevious();
                break;
            case CMD_PAUSE:
                //Toast.makeText(this, "pause",Toast.LENGTH_SHORT).show();
                Pause();
                break;
            case CMD_NEXT:
                //Toast.makeText(this, "next",Toast.LENGTH_SHORT).show();
                PlayNext();
                break;
            case CMD_ADD_NEXT:
            {
                //Toast.makeText(this, "addNext",Toast.LENGTH_SHORT).show();
                Song newSong = intent.getParcelableExtra("song");
                AddNext(newSong);
                break;
            }
            case CMD_ADD_TO_QUEUE:
            {
                //Toast.makeText(this, "addToQueue", Toast.LENGTH_SHORT).show();
                Song newSong = intent.getParcelableExtra("song");
                AddToQueue(newSong);
                break;
            }
            case CMD_REMOVE_FROM_PLAYLIST:
                int index = intent.getIntExtra("index",0);

                if(index < currentPlaying) {
                    currentPlaying--;
                    //queuePosition--;
                }

                playlist.remove(index);
                SendPlaylistAndReload();
                break;
            case CMD_REQUEST_IS_PLAYING:
                SendIsPlaying();
                break;
            case CMD_REQUEST_PLAYLIST:
                SendPlaylist();
                break;
            case CMD_PLAY_PLAYLIST_AT_INDEX:
                PlayAtIndex(intent.getIntExtra("index", 0));
                SendPlaylistAndReload();
                break;
            case CMD_EXIT:
                stopForeground(true);
                stopSelf();
                break;
            case -1:
            {
                if (intent.getAction().equals(PREV_ACTION))
                {
                    PlayPrevious();
                }
                else if (intent.getAction().equals(PAUSE_ACTION))
                {
                    Pause();
                }
                else if (intent.getAction().equals(NEXT_ACTION))
                {
                    PlayNext();
                }
            }
        }
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        mp.stop();
        mp.release();
        mp = null;
        isRunning = false;
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {

    }


    private void InitializeMediaPlayer() {
        mp = new MediaPlayer();

        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        WifiManager.WifiLock wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "musicWifiLock");

        wifiLock.acquire();

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                PlayNext();
            }
        });
    }

    private void SetUpPlaylist(List<Song> songlist, int pos) {
        //queuePosition = pos + 1;
        currentPlaying = pos;
        playlist.clear();
        for (Song s : songlist)
            playlist.add(s);
        SendPlaylist();
    }

    public void PlayFromSongAdapter(List<Song> songlist, int pos) {
        SetUpPlaylist(songlist, pos);
        Play(playlist.get(pos));
    }

    public void Play(Song song)
    {
        if (song.getDownlaoded()) {
            //Toast.makeText(this, Environment.getExternalStorageDirectory() + "/music/" + song.getFilePath(), Toast.LENGTH_SHORT).show();
            Play(Environment.getExternalStorageDirectory() + "/music/" + song.getFilePath());
        } else {
            //Toast.makeText(this, song.getMp3Url(), Toast.LENGTH_SHORT).show();
            Play(song.getMp3Url());
        }
    }
    public void Play(String url)
    {
        if (mp == null)
            InitializeMediaPlayer();
        else
            mp.reset();

        try {
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(url);
        } catch (IllegalArgumentException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IllegalStateException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mp.prepareAsync();
        } catch (IllegalStateException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        }/* catch (IOException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        }*/

        //pauseButton.setText("P");
    }
    public void PlayPrevious()
    {
        if (-1 < currentPlaying - 1) {
            currentPlaying--;
            Play(playlist.get(currentPlaying));
            NotifyPlayPrev();
        }
    }
    public void PlayNext()
    {

        if(queue.size() > 0)
        {
            Play(queue.get(0));
            queue.remove(0);
        }
        else
        {
            if (playlist.size() > currentPlaying + 1) {
                //queuePosition++;
                currentPlaying++;
                Play(playlist.get(currentPlaying));
                NotifyPlayNext();
            }
        }
    }
    public void PlayAtIndex(int index)
    {
        currentPlaying = index;
        Play(playlist.get(index));
    }
    public void Pause()
    {
        if (mp.isPlaying()) {
            mp.pause();
            //pauseButton.setText("p");
        } else {
            mp.start();
            //pauseButton.setText("P");
        }
    }
    public void AddNext(Song song)
    {
        playlist.add(currentPlaying+1, song);
        //queuePosition++;
        SendPlaylist();
    }
    public void AddToQueue(Song song)
    {
        queue.add(song);
        //playlist.add(queuePosition, song);
        //queuePosition++;
        //SendPlaylist();
    }



    public static final int BROADCAST_CMD_SEND_PLAYLIST = 0;
    public static final int BROADCAST_CMD_SEND_PLAYLIST_AND_RELOAD = 1;
    public static final int BROADCAST_CMD_SEND_IS_PLAYING = 2;
    public static final int BROADCAST_CMD_NOTIFY_PLAY_NEXT = 3;
    public static final int BROADCAST_CMD_NOTIFY_PLAY_PREV = 4;

    private void SendPlaylist() {
        Intent intent = new Intent("broadcast-event");
        intent.putExtra("cmd", BROADCAST_CMD_SEND_PLAYLIST);
        intent.putParcelableArrayListExtra("playlist", playlist);
        intent.putExtra("playingIndex", currentPlaying);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void SendPlaylistAndReload() {
        Intent intent = new Intent("broadcast-event");
        intent.putExtra("cmd", BROADCAST_CMD_SEND_PLAYLIST_AND_RELOAD);
        intent.putParcelableArrayListExtra("playlist", playlist);
        intent.putExtra("playingIndex", currentPlaying);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void SendIsPlaying() {
        Intent intent = new Intent("broadcast-event");
        intent.putExtra("cmd", BROADCAST_CMD_SEND_IS_PLAYING);
        intent.putExtra("isPlaying", mp.isPlaying());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void NotifyPlayNext() {
        Intent intent = new Intent("broadcast-event");
        intent.putExtra("cmd", BROADCAST_CMD_NOTIFY_PLAY_NEXT);
        intent.putExtra("toPlay", currentPlaying);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void NotifyPlayPrev() {
        Intent intent = new Intent("broadcast-event");
        intent.putExtra("cmd", BROADCAST_CMD_NOTIFY_PLAY_PREV);
        intent.putExtra("toPlay", currentPlaying);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}