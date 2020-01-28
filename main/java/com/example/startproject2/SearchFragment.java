package com.example.startproject2;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    MovieAdapter adapter;
    RecyclerView recyclerView;

    Movie movie;
    String clientId = "1o5FdWHChJv14R9eO8pE";
    String clientSecret = "nWwyegGVn4";
    static RequestQueue requestQueue;
    String urlStr = "https://openapi.naver.com/v1/search/movie.json?query=";
    String uriString = "content://com.example.startproject2.MovieProvider/movie";
    ViewGroup tempGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);

        adapter = new MovieAdapter();
        recyclerView = rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        adapter.clearItems();
        SearchView searchView = rootView.findViewById(R.id.searchView);
        tempGroup = rootView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                String text = "";
                try {
                    text = URLEncoder.encode(query, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                makeRequest(text);
                adapter.notifyDataSetChanged();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        movie = new Movie();

        movie.title = "movie title";
        movie.director = "director";
        movie.actor = "person1, pesron2, person3";
        movie.userRating = 5;

        adapter.rootActivity = rootView;
        requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        return rootView;
    }
    private void makeRequest(String text){
        final String temp = urlStr + text;
        StringRequest request = new StringRequest(Request.Method.GET, temp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                processResponse(response);
                Log.d("Request", "success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Request", "Error");
            }
        }) {@Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("X-Naver-Client-Id", clientId);
                params.put("X-Naver-Client-Secret", clientSecret);
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    private void processResponse(String response){
        Gson gson = new Gson();
        MovieList movieList = gson.fromJson(response, MovieList.class);
        adapter.clearItems();
        for(int i=0; i<movieList.items.size(); i++){
            Movie movie = movieList.items.get(i);
            adapter.addItem(movie);
        }
        clearMovie();
        if(movieList != null)
            insertMovie(movieList);
        adapter.notifyDataSetChanged();
    }
    private void insertMovie(MovieList movieList) {
        Uri uri = new Uri.Builder().build().parse(uriString);
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null,
                null, null);
        if(movieList.items.size() != 0) {
            for (int i = 0; i < movieList.items.size(); i++) {
                Movie movie = movieList.items.get(i);
                ContentValues values = new ContentValues();
                values.put("title", movie.title);
                values.put("director", movie.director);
                values.put("actor", movie.actor);
                values.put("link", movie.link);
                values.put("rating", movie.userRating);
                values.put("image", movie.image);
                values.put("pubDate", movie.pubDate);
                uri = getActivity().getContentResolver().insert(uri, values);
            }
        }
    }
    private void clearMovie() {
        Uri uri = new Uri.Builder().build().parse(uriString);
        int count = getActivity().getContentResolver().delete(uri, null, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        Uri uri = new Uri.Builder().build().parse(uriString);
        String[] columns = new String[] {"title", "director", "actor",
                "link", "rating", "image", "pubDate"};
        Cursor cursor = tempGroup.getContext().getContentResolver().query(
                uri, columns, null, null, "pubDate ASC");
        int count = cursor.getCount();
        for(int i=0; i<count; i++){
            Movie movie = new Movie();
            cursor.moveToNext();
            movie.title = cursor.getString(cursor.getColumnIndex(columns[0]));
            movie.director =cursor.getString(cursor.getColumnIndex(columns[1]));
            movie.actor = cursor.getString(cursor.getColumnIndex(columns[2]));
            movie.link = cursor.getString(cursor.getColumnIndex(columns[3]));
            movie.userRating = cursor.getFloat(cursor.getColumnIndex(columns[4]));
            movie.image = cursor.getString(cursor.getColumnIndex(columns[5]));
            movie.pubDate = cursor.getString(cursor.getColumnIndex(columns[6]));
            adapter.addItem(movie);
        }
    }
}
