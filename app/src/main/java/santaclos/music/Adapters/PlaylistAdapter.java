package santaclos.music.Adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import santaclos.music.MainActivity;
import santaclos.music.MusicService;
import santaclos.music.R;
import santaclos.music.Types.Song;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder> {

    private List<Song> songList;
    private MainActivity activity;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.name);
        }
    }


    public PlaylistAdapter(List<Song> albumList, MainActivity activity) {
        this.songList = albumList;
        this.activity = activity;
    }

    @Override
    public PlaylistAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.box, parent, false);

        return new PlaylistAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PlaylistAdapter.MyViewHolder holder, final int position) {
        final Song song = songList.get(position);
        holder.title.setText(song.getTitle());

        final boolean isPlaying;

        if(isPlaying=activity.playlistPlayingIndex==position)
            holder.itemView.setBackground(activity.getResources().getDrawable(R.drawable.dark_box_ripple));
        else
            holder.itemView.setBackground(activity.getResources().getDrawable(R.drawable.box_ripple));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(activity, MusicService.class);
                service.putExtra("cmd", MusicService.CMD_PLAY_PLAYLIST_AT_INDEX);
                service.putExtra("index", position);
                activity.startService(service);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(!isPlaying) {
                    AlertDialog.Builder aD = new AlertDialog.Builder(holder.itemView.getContext());

                    String[] buttons = {"Remove"};

                    aD.setItems(buttons, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0: {
                                    Intent service = new Intent(activity, MusicService.class);
                                    service.putExtra("cmd", MusicService.CMD_REMOVE_FROM_PLAYLIST);
                                    service.putExtra("index", position);
                                    activity.startService(service);
                                    break;
                                }
                            }
                        }
                    });
                    aD.show();
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }
}