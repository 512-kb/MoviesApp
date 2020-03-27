package com.example.movies.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.movies.Adapters.FavouritesAdapter;
import com.example.movies.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FavouritesActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_favourite);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        ((TextView) findViewById(R.id.heading)).setText("Favourite Movies");

        final JSONArray moviesArray = new JSONArray();

        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader bufferedReader = null;
                try {
                    File[] movies = getExternalFilesDir(null).listFiles();
                    for (File movie : movies) {
                        StringBuilder JSONString = new StringBuilder(); String line;
                        String details_path = movie.getPath() + "/details.json"; //poster_path = movie.getPath() + "/" + movieID + ".jpg", videos_path = movie.getPath() + "/videos.json";

                        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(details_path))));
                        while ((line = bufferedReader.readLine()) != null) {
                            JSONString.append(line);
                        }
                        moviesArray.put(new JSONObject(JSONString.toString()));
                    }

                RecyclerView favourites = findViewById(R.id.favourites);
                favourites.setLayoutManager(new LinearLayoutManager(FavouritesActivity.this));
                favourites.setAdapter(new FavouritesAdapter(getBaseContext(),moviesArray));

                } catch (Exception e) {
                    Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
                }
                finally {
                    try {
                        bufferedReader.close();
                    } catch (Exception e) {
                        Log.e("APP_ERROR", e.toString() + " " + e.getMessage());
                    }
                }
            }
        }).start();
    }

}
