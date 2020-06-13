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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sharmatushar.depressiondetector.Constants.NetworkLinks;
import com.sharmatushar.depressiondetector.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.UUID;

import static com.sharmatushar.depressiondetector.Constants.PreferenceKeys.DEVICE_ID;
import static com.sharmatushar.depressiondetector.Constants.PreferenceKeys.LOGIN_ID;

public class SplashScreen extends AppCompatActivity {

    private final String TAG = "SplashScreen";
    private SharedPreferences sharedPreferences;
    private long currTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView logoImage = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.appName);

        Animation topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        Animation bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        logoImage.startAnimation(topAnimation);
        appName.startAnimation(bottomAnimation);
        currTime = System.currentTimeMillis();

        Objects.requireNonNull(getSupportActionBar()).hide();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.contains(LOGIN_ID)) {
            String uniqueID = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(DEVICE_ID, uniqueID).apply();
            Log.d(TAG, "LoginId missing...");
            Log.d(TAG, "Creating Login id...");
            createUserId(uniqueID);
            // COMPLETED Add network request to fetch loginId
        } else {
            Log.d(TAG, "Found loginId: " + sharedPreferences.getString(LOGIN_ID, ""));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    finish();
                }
            }, 4000);
        }
    }

    private void createUserId(final String uniqueID) {
        String url = NetworkLinks.BASE_URL + NetworkLinks.LOGIN;
        Log.d(TAG, "Sending request to: " + url);
        JSONObject object = new JSONObject();
        try {
            object.put("DeviceId", uniqueID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Got response: " + response.toString());

                            String userId = response.getString("user_id");
                            sharedPreferences.edit().putString(LOGIN_ID, userId).apply();

                            long time = System.currentTimeMillis();
                            if (currTime - time >= 4000) {
                                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                finish();
                            } else {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                        finish();
                                    }
                                }, 4000 - (currTime - time));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Response Parse Error");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Got volley error.");
                        error.printStackTrace();
                    }
                });
        Volley.newRequestQueue(this).add(loginRequest);
    }
}