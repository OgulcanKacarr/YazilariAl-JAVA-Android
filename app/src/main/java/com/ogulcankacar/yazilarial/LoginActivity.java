package com.ogulcankacar.yazilarial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private ImageView imageView;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        imageView = findViewById(R.id.imageView);


        final SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences("com.ogulcankacar.yazilarial", MODE_PRIVATE);
        boolean trackboolen = sharedPreferences.getBoolean("trackboolen", false);


        if (trackboolen == false){
            loginFunc();
            sharedPreferences.edit().putBoolean("trackboolen",true).apply();
        }else{
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }





    }//OnCreate

    private void loginFunc(){

        imageView.setVisibility(View.GONE);
        mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.login);
        mediaPlayer.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                imageView.setVisibility(View.VISIBLE);
                imageView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim));
            }
        },1000);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();

            }
        },5000);
    }


}//Main