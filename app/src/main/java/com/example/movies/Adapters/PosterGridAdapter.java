package com.example.movies.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies.Activities.MovieDetailActivity;
import com.example.movies.OnBottomReachedListener;
import com.example.movies.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

public class PosterGridAdapter extends RecyclerView.Adapter<PosterGridAdapter.ViewHolder> {
    private JSONArray moviesList;
    private Context context;
    private OnBottomReachedListener onBottomReachedListener;

    public PosterGridAdapter(Context context, JSONArray list) {
        this.moviesList = list;
        this.context = context;
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
        this.onBottomReachedListener = onBottomReachedListener;
    }

    public void setMoviesList(JSONArray arr) {
        this.moviesList = arr;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new ImageView(context));
    }

    @Override
    public void onBindViewHolder(@NonNull PosterGridAdapter.ViewHolder holder, int position) {
        try {
            if (position == moviesList.length() - 1)
                onBottomReachedListener.onBottomReached(position);
            final ImageView moviePoster = holder.poster;
            final JSONObject movie = moviesList.getJSONObject(position);

            final String poster_url = context.getString(R.string.IMAGE_BASE_URL) + movie.getString("poster_path");

            Picasso.get().load(poster_url).into(moviePoster, new Callback() {
                @Override
                public void onSuccess() {
                    try {
                        moviePoster.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(context, MovieDetailActivity.class);
                                        try {
                                            intent.putExtra("id", movie.getInt("id") + "");
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.startActivity(intent);
                                        } catch (Exception e) {
                                            Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
                    }

                }

                @Override
                public void onError(Exception e) {
                    Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return moviesList.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView poster;

        public ViewHolder(ImageView img) {
            super(img);
            img.setScaleType(ImageView.ScaleType.FIT_XY);
            img.setPadding(5, 5, 5, 5);
            this.poster = img;
        }
    }

}
