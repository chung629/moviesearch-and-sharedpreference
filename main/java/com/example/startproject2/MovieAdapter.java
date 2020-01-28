package com.example.startproject2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>{

    ArrayList<Movie> items = new ArrayList<Movie>();
    ViewGroup rootActivity;
    String imgUrl;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.movie_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Movie item = items.get(position);

        holder.setItem(item);
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Movie item){
        items.add(item);
    }

    public void setItems(ArrayList<Movie> items){
        this.items = items;
    }

    public Movie getItem(int position) {
        return items.get(position);
    }

    public void clearItems() {
        this.items.clear();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView, textView2, textView3, textView4;
        ImageView imageView;
        RatingBar ratingBar;
        String link;

        public final View layout;

        Bitmap bitmap;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textView);
            textView2 = itemView.findViewById(R.id.textView2);
            textView3 = itemView.findViewById(R.id.textView3);
            textView4 = itemView.findViewById(R.id.textView4);
            imageView = itemView.findViewById(R.id.imageView);
            ratingBar = itemView.findViewById(R.id.ratingBar);

            layout = itemView;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    Log.d("Link", link);
                    rootActivity.getContext().startActivity(intent);
                }
            });
        }

        public void setItem(Movie item) {
            String tempTitle = item.title;
            String resultTitle = "";
            for(int i=0; i<tempTitle.length(); i++){
                if(tempTitle.charAt(i) == '<' || tempTitle.charAt(i) == 'b' ||tempTitle.charAt(i) == '>' ||
                        tempTitle.charAt(i) == '/');
                else
                    resultTitle+=tempTitle.charAt(i);
            }
            resultTitle += " (" + item.pubDate + ")";
            textView.setText(resultTitle);
            link = item.link;
            String tempDirector = "";
            for(int i=0; i<item.director.length(); i++)
                if(item.director.charAt(i) != '|')
                    tempDirector+=item.director.charAt(i);
            textView2.setText("감독: " + tempDirector);
            String tempActor = "";
            for(int i=0; i<item.actor.length(); i++)
                if(item.actor.charAt(i) == '|') {
                    if (i != item.actor.length()-1)
                        tempActor += ", ";
                } else
                    tempActor+=item.actor.charAt(i);
            textView3.setText("출연: " + tempActor);

            if (item.userRating != 0) {
                textView4.setText(item.userRating + " ");
                ratingBar.setVisibility(View.VISIBLE);
                ratingBar.setRating(item.userRating / 2);
            } else {
                textView4.setText("평점 없음");
                ratingBar.setVisibility(View.GONE);
            }
            imgUrl = item.image;
            {
                Thread tempThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            URL url = new URL(imgUrl);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setDoInput(true);
                            conn.connect();
                            InputStream is = conn.getInputStream();
                            bitmap = BitmapFactory.decodeStream(is);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                tempThread.start();
                try {
                    tempThread.join();
                    if(bitmap == null)
                        imageView.setImageResource(R.drawable.movie);
                    else
                        imageView.setImageBitmap(bitmap);
                } catch (Exception e) { }
            }
        }
    }
}
