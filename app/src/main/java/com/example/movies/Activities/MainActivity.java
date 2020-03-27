package com.example.movies.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.movies.Adapters.PosterGridAdapter;
import com.example.movies.OnBottomReachedListener;
import com.example.movies.R;
import com.example.movies.VolleyInstance;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private int page = 0, pageMax = 2;
    private RecyclerView gridView;
    private String currentFilter = "popular";
    private RequestQueue requestQueue;
    private PosterGridAdapter adapter;
    private GridLayoutManager layoutManager;
    private JSONArray moviesArray;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        try {
            ActionBar actionBar = getSupportActionBar();
            assert actionBar != null;
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.action_bar);
            ((TextView) findViewById(R.id.heading)).setText("Popular Movies");
            findViewById(R.id.fav_text).setVisibility(View.VISIBLE);

            requestQueue = VolleyInstance.getInstance(this.getApplicationContext()).getRequestQueue();
            moviesArray = new JSONArray();
            layoutManager = new GridLayoutManager(this, 2);

            gridView = findViewById(R.id.gridView);
            gridView.setLayoutManager(layoutManager);

            adapter = new PosterGridAdapter(getBaseContext(), moviesArray);
            adapter.setOnBottomReachedListener(new OnBottomReachedListener() {
                @Override
                public void onBottomReached(int position) {
                    fetchMovies();
                }
            });
            gridView.setAdapter(adapter);
            fetchMovies();
        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
        }
    }

    public void onFavsClick(View v){
        startActivity(new Intent(this,FavouritesActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String newFilter = getFilter(item.getItemId());
        assert newFilter != null;
        if (newFilter.equals(currentFilter)) return super.onOptionsItemSelected(item);
        try {
            page = 0;
            pageMax = 2;
            moviesArray = new JSONArray();
            currentFilter = newFilter;
            fetchMovies();
            ((TextView) findViewById(R.id.heading)).setText(item.getTitle() + " Movies");
        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutManager.setSpanCount(4);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager.setSpanCount(2);
        }
    }

    private void updateUI(int prevLength, int length) {
        try {
            if (moviesArray.length() < 1)
                return; //{Toast.makeText(this,"Not Found",Toast.LENGTH_SHORT).show(); return;}

            adapter.setMoviesList(moviesArray);
            if (prevLength == 0) {
                adapter.notifyDataSetChanged();
                gridView.scrollToPosition(0);
            } else adapter.notifyItemRangeInserted(prevLength, length - prevLength);
        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
        }
    }

    public void fetchMovies() {
        if (++page > pageMax) return;
        final String url =
                getString(R.string.API_BASE_URL) + "/movie/" + currentFilter + "?api_key=" + getString(R.string.API_KEY) + "&page=" + page;
        JsonObjectRequest req =
                new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject res) {
                                try {
                                    //Log.e("APP_ERROR",currentFilter+"\n"+res.toString()+"\n"+url+"\n\n");

                                    pageMax = res.getInt("total_pages");
                                    String reviewsToString = moviesArray.toString(),
                                            resToString = res.getJSONArray("results").toString();
                                    int len = moviesArray.length();
                                    if (moviesArray.length() == 0)
                                        moviesArray = res.getJSONArray("results");
                                    else
                                        moviesArray = new JSONArray(reviewsToString.substring(0, reviewsToString.length() - 2) + "},{" + resToString.substring(2));

                                    //Log.e("APP_ERROR",len+" --> "+moviesArray.length());
                                    updateUI(len, moviesArray.length());
                                } catch (Exception e) {
                                    Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError err) {
                                Log.e("APP_ERROR", err.toString() + " " + err.getMessage());
                            }
                        });
        requestQueue.add(req);
    }

    private String getFilter(int id) {
        switch (id) {
            case R.id.popular:
                return "popular";
            case R.id.now_playing:
                return "now_playing";
            case R.id.top_rated:
                return "top_rated";
            case R.id.upcoming:
                return "upcoming";
        }
        return null;
    }
}
