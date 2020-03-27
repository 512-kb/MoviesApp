package com.example.movies.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies.OnBottomReachedListener;
import com.example.movies.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {


    private JSONArray reviewsArray;
    private OnBottomReachedListener onBottomReachedListener;

    public ReviewsAdapter(JSONArray arr) {
        setReviewsArray(arr);
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
        this.onBottomReachedListener = onBottomReachedListener;
    }

    public void setReviewsArray(JSONArray arr) {
        this.reviewsArray = arr;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.review_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == reviewsArray.length() - 1)
            onBottomReachedListener.onBottomReached(position);
        try {
            JSONObject review = reviewsArray.getJSONObject(position);
            final TextView content = holder.content, username = holder.username;
            content.setText(review.getString("content"));
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    content.setMaxLines(content.getMaxLines()==4?Integer.MAX_VALUE:4);
                }
            });
            username.setText(review.getString("author"));
        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString());
        }
    }

    @Override
    public int getItemCount() {
        return reviewsArray.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username, content;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            content = itemView.findViewById(R.id.content);
        }
    }
}

