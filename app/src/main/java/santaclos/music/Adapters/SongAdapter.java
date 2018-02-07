package santaclos.music.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import santaclos.music.MainActivity;
import santaclos.music.MusicService;
import santaclos.music.R;
import santaclos.music.SongManagement;
import santaclos.music.Types.Song;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    private ArrayList<Song> songList;
    private MainActivity activity;
    private SharedPreferences.Editor prefsEditor;
    Typeface musicIconsTypeface;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView number;
        public TextView downloaded;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.name);
            number = (TextView) view.findViewById(R.id.number);
            downloaded = (TextView) view.findViewById(R.id.downloaded);
        }
    }


    public SongAdapter(ArrayList<Song> albumList, MainActivity activity, SharedPreferences.Editor prefsEditor, Typeface t) {
        this.songList = albumList;
        this.activity = activity;
        this.prefsEditor = prefsEditor;
        musicIconsTypeface = t;
    }

    @Override
    public SongAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_box, parent, false);

        return new SongAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SongAdapter.MyViewHolder holder, final int position) {
        final Song song = songList.get(position);
        holder.title.setText(song.getTitle());
        holder.number.setText(String.valueOf(song.getNumber()));
        holder.downloaded.setText(song.getDownlaoded()?"D":"");
        holder.downloaded.setTypeface(musicIconsTypeface);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent service = new Intent(activity, MusicService.class);

                service.putExtra("cmd", MusicService.CMD_PLAY_FROM_SONG_ADAPTER);
                service.putParcelableArrayListExtra("songList", songList);
                service.putExtra("position", position);
                activity.startService(service);

                activity.ShowControlButtons();
                activity.SetPauseButton();
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder aD = new AlertDialog.Builder(holder.itemView.getContext());

                String[] buttons = {"Add next", "Add to queue", "Lyrics", "Download"};
                if(song.getDownlaoded())buttons[3]="Delete";

                aD.setItems(buttons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which)
                        {
                            case 0:
                            {
                                Intent service = new Intent(activity, MusicService.class);

                                service.putExtra("cmd", MusicService.CMD_ADD_NEXT);
                                service.putExtra("song", song);
                                activity.startService(service);
                                break;
                            }
                            case 1:
                            {
                                Intent service = new Intent(activity, MusicService.class);

                                service.putExtra("cmd", MusicService.CMD_ADD_TO_QUEUE);
                                service.putExtra("song", song);
                                activity.startService(service);
                                break;
                            }
                            case 2:
                            {
                                LoadLyrics(song.getArtist(), song.getAlbum(), song.getTitle(), holder.itemView.getContext());
                                break;
                            }
                            case 3:
                            {
                                if(song.getDownlaoded()){
                                    SongManagement.DeleteSong(activity, prefsEditor, song, true);
                                }
                                else {
                                    SongManagement.DownloadSong(activity, prefsEditor, song,true);
                                }
                                break;
                            }
                        }
                    }
                });
                aD.show();
                return true;
            }
        });
    }



    @Override
    public int getItemCount() {
        return songList.size();
    }

    private void LoadLyrics(String artist, String album, final String song, final Context context){
        String phpString = "http://lc.pe.hu/lyricsGetter.php?data=";
        phpString+=FixStringForURL(artist)+'\\'+FixStringForURL(album)+'\\'+FixStringForURL(song);
        //String phpString = "https://stackoverflow.com/questions/6503574/how-to-get-html-source-code-from-url-in-android";
        //Toast.makeText(context, phpString, Toast.LENGTH_LONG).show();

        Ion.with(context).load(phpString).asString().setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(song);
                alertDialog.setMessage(result);
                alertDialog.show();
            }
        });
    }

    static String FixStringForURL(String input){
        String stringout = "";
        for(int i = 0; i < input.length();i++)
        {
            if(input.charAt(i)==' ')
                stringout += "%20";
            else
                stringout += input.charAt(i);
        }
        return stringout;
    }
    /*private String GetParentFolder(String path)
    {
        return path.substring(0, path.lastIndexOf('/')+1);
    }*/
}
