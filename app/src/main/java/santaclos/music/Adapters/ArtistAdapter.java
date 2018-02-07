package santaclos.music.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import santaclos.music.MainActivity;
import santaclos.music.R;
import santaclos.music.Types.Artist;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.MyViewHolder> {

    private List<Artist> artistList;
    private MainActivity activity;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.name);
        }
    }


    public ArtistAdapter(List<Artist> artistList, MainActivity activity) {
        this.artistList = artistList;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.box, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Artist artist = artistList.get(position);
        holder.title.setText(artist.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.LoadAlbums(artist.getAlbumsURL());
            }
        });
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }
}