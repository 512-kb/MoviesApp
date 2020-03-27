package com.example.movies.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies.DateParser;
import com.example.movies.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.ViewHolder> {

    private JSONArray moviesArray;
    private Context context;

    public FavouritesAdapter(Context context, JSONArray arr) {
        setReviewsArray(arr);
        this.context = context;
    }

    public void setReviewsArray(JSONArray arr) {
        this.moviesArray = arr;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fav_view, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        try {
            JSONObject movie = moviesArray.getJSONObject(position);
            final String movieID = movie.getInt("id") + "";
            holder.title.setText(movie.getString("title"));
            holder.rating.setText("Rating: " + movie.getDouble("vote_average") + "/10");
            holder.releaseDate.setText("Release Date: " + DateParser.parseDate(movie.getString("release_date")));
            holder.poster.setImageURI(Uri.fromFile(new File(context.getExternalFilesDir(null) + "/" + movieID + "/" + movieID + ".jpg")));
            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    deleteMovie(movieID);
                    moviesArray.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position,moviesArray.length()-position);
                }
            });
        } catch (Exception e) {
            Log.e("APP_ERROR", "BindError --> " + e.toString());
        }
    }

    @Override
    public int getItemCount() {
        return moviesArray.length();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, rating, releaseDate;
        ImageView poster;
        ImageButton deleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.fav_title);
            rating = itemView.findViewById(R.id.fav_rating);
            releaseDate = itemView.findViewById(R.id.fav_release_date);
            poster = itemView.findViewById(R.id.fav_poster);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }

    public void deleteMovie(String id) {
        try {
            File dir = new File(context.getExternalFilesDir(null) + "/" + id);
            if (dir.exists()) Runtime.getRuntime().exec("rm -rf " + dir);
        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
        }
    }

}

