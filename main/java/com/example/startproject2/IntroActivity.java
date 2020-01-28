package com.example.startproject2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        imageView = findViewById(R.id.imageView2);
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.introstart);
        imageView.startAnimation(anim);
        textView = findViewById(R.id.textView5);
        Animation anim2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.introstart2);
        textView.startAnimation(anim2);

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
                finish();
            }
        };
        handler.sendEmptyMessageDelayed(0,2000);
    }
}
