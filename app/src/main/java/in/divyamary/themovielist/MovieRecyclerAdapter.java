package in.divyamary.themovielist;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by divyamary on 31-12-2015.
 */
public class MovieRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final static String MOVIE = "in.divyamary.sunshine.MOVIE";
    private List<Movie> mMovieList;

    MovieRecyclerAdapter(List<Movie> movieList) {
        this.mMovieList = movieList;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public CardView cardView;
        public ImageView imageView;
        private Context context;

        public ImageViewHolder(Context context, View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.grid_item_image);
            this.context = context;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent movieDetailIntent = new Intent(context, MovieDetailActivity.class);
            int position = getLayoutPosition();
            movieDetailIntent.putExtra(MOVIE, mMovieList.get(position).getMovieId());
            context.startActivity(movieDetailIntent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_image, parent, false);
        RecyclerView.ViewHolder viewHolder = new ImageViewHolder(parent.getContext(), view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Movie movie = mMovieList.get(position);
        Context context = ((ImageViewHolder)holder).imageView.getContext();
        Picasso.with(context).load(movie.getMoviePosterURL()).into(((ImageViewHolder)holder).imageView);
    }

    public void addData(List<Movie> moviesList) {
        mMovieList.clear();
        mMovieList.addAll(moviesList);
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
