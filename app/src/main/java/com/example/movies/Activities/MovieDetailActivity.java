package com.example.movies.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.movies.Adapters.ReviewsAdapter;
import com.example.movies.DateParser;
import com.example.movies.OnBottomReachedListener;
import com.example.movies.R;
import com.example.movies.VolleyInstance;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

public class MovieDetailActivity extends AppCompatActivity {
    int page = 0, pageMax = 2;
    private RequestQueue requestQueue;
    private ReviewsAdapter adapter;
    private String movieID;
    private JSONObject movie;
    private TextView movieTitle, movieOverview, movieReleaseDate, movieRating;
    private LinearLayout videos;
    private JSONArray videoArray, reviewsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        movieID = getIntent().getExtras().getString("id");

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        ((TextView) findViewById(R.id.heading)).setText("Movie Details");
        findViewById(R.id.star).setVisibility(View.VISIBLE);

        ToggleButton toggle = findViewById(R.id.star);
        if (new File(getExternalFilesDir(null) + "/" + movieID).exists()) toggle.setChecked(true);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) onStarOn();
                else onStarOff();
            }
        });

        requestQueue = VolleyInstance.getInstance(this.getApplicationContext()).getRequestQueue();
        movieTitle = findViewById(R.id.movieTitle);
        movieOverview = findViewById(R.id.movieOverview);
        movieReleaseDate = findViewById(R.id.movieReleaseDate);
        movieRating = findViewById(R.id.movieRating);
        videos = findViewById(R.id.videos);
        reviewsArray = new JSONArray();

        RecyclerView reviews = findViewById(R.id.reviewsList);

        adapter = new ReviewsAdapter(reviewsArray);
        adapter.setOnBottomReachedListener(new OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                fetchReviews();
            }
        });

        reviews.setAdapter(adapter);
        reviews.setLayoutManager(new LinearLayoutManager(this));

        fetchDetails();
        fetchVideos();
        fetchReviews();
    }

    private void updateUI() {
        try {
            if (movie.getString("homepage").length() < 1)
                findViewById(R.id.homepageLink).setVisibility(View.GONE);
            movieTitle.setText(movie.getString("title").toUpperCase());
            if (movie.getString("overview") == null) movieOverview.setVisibility(View.GONE);
            else movieOverview.setText(movie.getString("overview"));
            movieReleaseDate.setText("Release Date: " + DateParser.parseDate(movie.getString("release_date")));
            movieRating.setText("Rating: " + movie.getDouble("vote_average") + "/10");

        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
        }
    }

    private void updateVideoList() {
        if (videoArray == null || videoArray.length() <= 0) {
            findViewById(R.id.videos_trailers).setVisibility(View.GONE);
            return;
        }
        for (int i = 0; i < videoArray.length(); i++) {
            try {
                JSONObject video = videoArray.getJSONObject(i);
                if (!video.getString("site").equals("YouTube")) {
                    videoArray.remove(i);
                    i--;
                    continue;
                }
                final String title = video.getString("name"), key = video.getString("key");
                String thumbnailURL = getString(R.string.YOUTUBE_THUMBNAIL).replace("VID_ID", key);
                final String videoURL = getString(R.string.YOUTUBE_URL) + key;
                final View videoTile = LayoutInflater.from(this).inflate(R.layout.video_view, null);
                final ImageView thumbnail = videoTile.findViewById(R.id.thumbnail);

                final int finalI = i;
                Picasso.get().load(thumbnailURL).into(thumbnail, new Callback() {
                    @Override
                    public void onSuccess() {
                        ((TextView) videoTile.findViewById(R.id.title)).setText(title);
                        videoTile.findViewById(R.id.shareBtn).setTag(videoURL);
                        videoTile.findViewById(R.id.playBtn).setTag(videoURL);
                        videoTile.setVisibility(View.VISIBLE);

                        videos.addView(videoTile);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("APP_ERROR", finalI + " Picasso---> " + e.toString() + " " + e.getMessage() + " " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
            }
        }
    }

    private void updateReviews(int prevLength, int length) {
        if (length < 1) {
            findViewById(R.id.reviews).setVisibility(View.GONE);
            return;
        }
        adapter.setReviewsArray(reviewsArray);
        adapter.notifyItemRangeInserted(prevLength, length - prevLength);
    }

    protected void fetchDetails() {
        final String url =
                getString(R.string.API_BASE_URL)
                        + "/movie/"
                        + movieID
                        + "?api_key="
                        + getString(R.string.API_KEY);
        JsonObjectRequest req =
                new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject res) {
                                movie = res;
                                updateUI();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError err) {
                                Log.e("APP_ERROR", err.toString());
                            }
                        });
        requestQueue.add(req);
    }

    protected void fetchVideos() {
        final String url = getString(R.string.MOVIE_VIDEOS_URL).replace("MOVIE_ID", movieID)
                + getString(R.string.API_KEY);
        JsonObjectRequest req =
                new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject res) {
                                try {
                                    videoArray = res.getJSONArray("results");
                                    updateVideoList();
                                } catch (Exception e) {
                                    Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError e) {
                                Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
                            }
                        });
        requestQueue.add(req);
    }

    protected void fetchReviews() {
        if (++page > pageMax) return;
        final String url = getString(R.string.MOVIE_REVIEWS_URL).replace("MOVIE_ID", movieID)
                + getString(R.string.API_KEY) + "&page=" + page;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        try {
                            pageMax = res.getInt("total_pages");
                            int len = reviewsArray.length();
                            String reviewsToString = reviewsArray.toString(),
                                    resToString = res.getJSONArray("results").toString();
                            if (reviewsArray.length() == 0)
                                reviewsArray = res.getJSONArray("results");
                            else
                                reviewsArray = new JSONArray(reviewsToString.substring(0, reviewsToString.length() - 2) + "},{" + resToString.substring(2));
                            updateReviews(len, reviewsArray.length());
                        } catch (Exception e) {
                            Log.e("APP_ERROR", e.toString() + " \n" + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Log.e("APP_ERROR", e.toString() + " \n" + e.getMessage());
            }
        });
        requestQueue.add(req);
    }

    final public void watchYoutubeVideo(View btn) {
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("" + btn.getTag()));
        startActivity(webIntent);
    }

    final public void visitWebsite(View btn) {
        try {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(movie.getString("homepage")));
            startActivity(webIntent);
        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
        }
    }

    final public void shareVideoLink(View btn) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Movies App");
            String shareMessage = "Watch this movie trailer\n" + btn.getTag();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share Link"));
        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString() + " " + e.getMessage() + " " + e.getMessage());
        }
    }

    private void onStarOn() {
            saveMovie();
    }

    private void onStarOff() {
        try {
            File dir = new File(getExternalFilesDir(null) + "/" + movieID);
            if (dir.exists()) Runtime.getRuntime().exec("rm -rf " + dir);
        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
        }
    }

    public void saveImage(final String url, final String sub_dir, final String img_name) {
        try {
            Picasso.get().load(url).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    try {
                        String path = getExternalFilesDir(null) + "/" + movieID + sub_dir;
                        String name = img_name + ".jpg";
                        File myDir = new File(path);

                        if (!myDir.exists()) {
                            myDir.mkdirs();
                        }
                        myDir = new File(myDir, name);
                        FileOutputStream out = new FileOutputStream(myDir);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        Log.e("APP_ERROR", "ImgSave --> " + e.toString() + " " + e.getMessage());
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Log.e("APP_ERROR", "Picasso --> " + e.toString() + " " + e.getMessage());
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
        }
    }

    public void saveMovie() {
        if (videoArray == null || videoArray.length() <= 0) return;
        try {
            final String poster_url = getString(R.string.IMAGE_BASE_URL) + movie.getString("poster_path");
            saveImage(poster_url, "/", movieID);

//            for (int i = 0; i < videoArray.length(); i++) {
//                JSONObject video = videoArray.getJSONObject(i);
//                final String title = video.getString("name"), key = video.getString("key"),
//                        thumbnailURL = getString(R.string.YOUTUBE_THUMBNAIL).replace("VID_ID", key);
//                saveImage(thumbnailURL, "/thumbnails", title);
//            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String movie_path = getExternalFilesDir(null) + "/" + movieID;
                        File movie_dir = new File(movie_path);
                        if (!movie_dir.exists()) movie_dir.mkdirs();
                        movie_dir = new File(movie_path, "details.json");

                        FileOutputStream fos = new FileOutputStream(movie_dir);
                        fos.write(movie.toString().getBytes());

//                        movie_dir = new File(movie_path, "videos.json");
//                        fos = new FileOutputStream(movie_dir);
//                        fos.write(videoArray.toString().getBytes());

                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        Log.e("APP_ERROR", "MovieThread --> " + e.toString() + " " + e.getMessage());
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.e("APP_ERROR", "MovieSave --> " + e.toString() + " " + e.getMessage());
        }
    }
}
