package com.sharmatushar.depressiondetector.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sharmatushar.depressiondetector.R;

import java.util.Objects;
import java.util.UUID;

import static com.sharmatushar.depressiondetector.Constants.PreferenceKeys.DEVICE_ID;

public class SplashScreen extends AppCompatActivity {

    private ImageView logoImage;
    private TextView appName;
    private Animation topAnimation, bottomAnimation;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logoImage = findViewById(R.id.logo);
        appName = findViewById(R.id.appName);

        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        logoImage.startAnimation(topAnimation);
        appName.startAnimation(bottomAnimation);

        Objects.requireNonNull(getSupportActionBar()).hide();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.contains(DEVICE_ID)) {
            String uniqueID = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(DEVICE_ID, uniqueID).apply();
            Log.d("SplashScreen", uniqueID);
            // TODO Add network request to fetch loginId
        } else {
            Log.d("SplashScreen", sharedPreferences.getString(DEVICE_ID, ""));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finish();
            }
        }, 4000);
    }
}