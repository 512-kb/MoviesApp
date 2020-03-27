package com.example.movies;

import android.util.Log;

import java.text.SimpleDateFormat;

public class DateParser {

    public static String parseDate(String DateString) {
        String str = "";
        try {
            str = new SimpleDateFormat("d MMMM,yyyy")
                    .format(new SimpleDateFormat("yyyy-MM-dd").parse(DateString));
        } catch (Exception e) {
            Log.e("APP_ERROR", e.toString() + " " + e.getMessage() + " " + e.getMessage());
        }
        return str;
    }
}
