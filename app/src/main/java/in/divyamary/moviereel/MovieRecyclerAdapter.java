package in.divyamary.moviereel;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

class MovieRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String MOVIE = "in.divyamary.MOVIE";
    private List<Movie> mMovieList;

    MovieRecyclerAdapter(List<Movie> movieList) {
        this.mMovieList = movieList;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
            if (Utils.isInternetConnected(context)) {
                context.startActivity(movieDetailIntent);
            } else {
                Snackbar snackbar = Snackbar.make(((AppCompatActivity) context).findViewById(R.id.recycler_view),
                        context.getString(R.string.no_internet),
                        Snackbar.LENGTH_LONG);
                View snackBarView = snackbar.getView();
                snackBarView.setBackgroundColor(context.getResources().getColor(R.color.grey_900));
                snackbar.show();
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_image, parent, false);
        return new ImageViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Movie movie = mMovieList.get(position);
        Context context = ((ImageViewHolder)holder).imageView.getContext();
        Picasso.with(context)
                .load(movie.getMoviePosterURL())
                .into(((ImageViewHolder) holder).imageView);
    }

    public void addData(List<Movie> moviesList, Boolean isRefresh) {
        if (isRefresh) {
            mMovieList.clear();
        }
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
