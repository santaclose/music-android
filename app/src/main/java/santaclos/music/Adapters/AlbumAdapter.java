package santaclos.music.Adapters;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.List;

import santaclos.music.MainActivity;
import santaclos.music.R;
import santaclos.music.SongManagement;
import santaclos.music.Types.Album;
import santaclos.music.Types.Song;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyViewHolder> {

    private List<Album> albumList;
    private MainActivity activity;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView artwork;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            artwork = (ImageView) view.findViewById(R.id.artwork);
        }
    }


    public AlbumAdapter(List<Album> albumList, MainActivity activity, SharedPreferences.Editor prefsEditor, SharedPreferences prefs) {
        this.albumList = albumList;
        this.activity = activity;
        this.prefsEditor = prefsEditor;
        this.prefs = prefs;
    }

    @Override
    public AlbumAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_box, parent, false);

        return new AlbumAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AlbumAdapter.MyViewHolder holder, int position) {
        final Album album = albumList.get(position);
        holder.title.setText(album.getTitle());

        //Toast.makeText(holder.itemView.getContext(), album.getArtworkURL(), Toast.LENGTH_SHORT).show();
        Glide.with(holder.itemView.getContext()).load(album.getArtworkURL()).into(holder.artwork);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.LoadSongs(album.getSongListURL());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder aD = new AlertDialog.Builder(holder.itemView.getContext());

                String[] buttons = {"Download","Delete"};

                aD.setItems(buttons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which)
                        {
                            case 0:
                            {
                                DownloadAlbumSongs(album.getSongListURL());
                                break;
                            }
                            case 1:
                            {
                                DeleteAlbumSongs(album.getSongListURL());
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

    private void DownloadAlbumSongs(String url){
        Ion.with(activity.getApplicationContext())
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        String lineSplit[] = result.split("\\n");
                        String[] artistAlbum = lineSplit[0].split("\\\\");
                        for (int i = 1; i < lineSplit.length; i++) {
                            String split[] = lineSplit[i].split("\\\\");
                            Song current = new Song(artistAlbum[0], artistAlbum[1], split[0], split[1], split.length > 2 ? split[2] : "", i, prefs);
                            if(!current.getDownlaoded())
                                SongManagement.DownloadSong(activity,prefsEditor,current,false);
                        }
                    }
                });
    }
    private void DeleteAlbumSongs(String url){
        Ion.with(activity.getApplicationContext())
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        String lineSplit[] = result.split("\\n");
                        String[] artistAlbum = lineSplit[0].split("\\\\");
                        for (int i = 1; i < lineSplit.length; i++) {
                            String split[] = lineSplit[i].split("\\\\");
                            Song current = new Song(artistAlbum[0], artistAlbum[1], split[0], split[1], split.length > 2 ? split[2] : "", i, prefs);
                            if(current.getDownlaoded())
                                SongManagement.DeleteSong(activity,prefsEditor,current,false);
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}
