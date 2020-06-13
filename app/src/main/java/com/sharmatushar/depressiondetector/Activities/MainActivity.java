package com.sharmatushar.depressiondetector.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.sharmatushar.depressiondetector.R;
import com.sharmatushar.depressiondetector.Services.ActivityCollectionService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import static com.sharmatushar.depressiondetector.Constants.NetworkLinks.BASE_URL;
import static com.sharmatushar.depressiondetector.Constants.NetworkLinks.DEPRESSION_STATE_LAST_WEEK;
import static com.sharmatushar.depressiondetector.Constants.NetworkLinks.DEPRESSION_STATE_TODAY;
import static com.sharmatushar.depressiondetector.Constants.PreferenceKeys.LOGIN_ID;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private final String TAG = "MainActivity";

    private DrawerLayout drawer;
    private TextView currentDayScore, previousWeekScore, stopRecording;
    private ImageView scoreDescriptor;
    private ProgressBar progressBarToday, progressBarLastWeek;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing values here
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer,
                null,
                R.string.app_name,
                R.string.app_name);
        NavigationView navigationView = findViewById(R.id.navigation);
        ImageView toolbarMenuIcon = findViewById(R.id.menu_icon);
        Window window = getWindow();
        stopRecording = findViewById(R.id.time_display);
        currentDayScore = findViewById(R.id.depression_score);
        previousWeekScore = findViewById(R.id.overall_depression_score);
        scoreDescriptor = findViewById(R.id.score_depict);
        progressBarToday = findViewById(R.id.progressbar_today);
        progressBarLastWeek = findViewById(R.id.progressbar_last_week);
        RelativeLayout depressionToday = findViewById(R.id.depression_today_layout);
        RelativeLayout depressionOverall = findViewById(R.id.overall_score_layout);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //setting values here
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        Objects.requireNonNull(getSupportActionBar()).hide();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.status_bar_color));
        // Status bar decorator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Storage Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent serviceIntent = new Intent(this, ActivityCollectionService.class);
                serviceIntent.setAction("START");
                ContextCompat.startForegroundService(this, serviceIntent);
                stopRecording.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (stopRecording.getText().toString().contains("stop")) {
                            Intent serviceIntent = new Intent(MainActivity.this, ActivityCollectionService.class);
                            serviceIntent.setAction("STOP");
                            ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
                            stopRecording.setText(R.string.start_activity_recording);
                        } else {
                            Intent serviceIntent = new Intent(MainActivity.this, ActivityCollectionService.class);
                            serviceIntent.setAction("START");
                            ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
                            stopRecording.setText(R.string.stop_activity_recording);
                        }
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE },
                        1);
            }
        }

        //Listeners here
        drawer.addDrawerListener(toggle);
        toolbarMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        // Today's depression score
        depressionToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLastDayDepressionScore(sharedPreferences.getString(LOGIN_ID, ""));
            }
        });

        // Overall depression score
        depressionOverall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLastWeekDepressionScore(sharedPreferences.getString(LOGIN_ID, ""));
            }
        });

        // Network Calls here
        requestLastDayDepressionScore(sharedPreferences.getString(LOGIN_ID, ""));
        requestLastWeekDepressionScore(sharedPreferences.getString(LOGIN_ID, ""));
    }

    /**
     * Check the depression state of user today.
     * @param login_id user login id
     */
    private void requestLastDayDepressionScore(String login_id) {
        String url = BASE_URL + DEPRESSION_STATE_TODAY;

        progressBarToday.setVisibility(View.VISIBLE);
        currentDayScore.setVisibility(View.VISIBLE);
        scoreDescriptor.setVisibility(View.GONE);

        currentDayScore.setText(R.string.initial_message);
        JSONObject object = new JSONObject();
        try {
            object.put("UserId", login_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest todaysStateRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Got daily response: " + response.toString());
                            String state = response.getString("status");
                            if (state.equals("Not Depressed")) {
                                progressBarToday.setVisibility(View.GONE);
                                currentDayScore.setVisibility(View.VISIBLE);
                                scoreDescriptor.setVisibility(View.VISIBLE);

                                scoreDescriptor.setImageDrawable(getDrawable(R.drawable.ic_mind_mapping));
                                currentDayScore.setText(R.string.non_depression_text_daily);
                            } else if (state.equals("Depressed")) {
                                progressBarToday.setVisibility(View.GONE);
                                currentDayScore.setVisibility(View.VISIBLE);
                                scoreDescriptor.setVisibility(View.VISIBLE);

                                scoreDescriptor.setImageDrawable(getDrawable(R.drawable.ic_depression));
                                currentDayScore.setText(R.string.depression_text_daily);
                            } else {
                                progressBarToday.setVisibility(View.GONE);
                                currentDayScore.setVisibility(View.VISIBLE);

                                currentDayScore.setText(R.string.no_depression_score);
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "Cannot parse response");
                            progressBarToday.setVisibility(View.GONE);
                            currentDayScore.setVisibility(View.VISIBLE);

                            currentDayScore.setText(R.string.error_depression_score);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Volley error");
                        error.printStackTrace();
                        progressBarToday.setVisibility(View.GONE);
                        currentDayScore.setVisibility(View.VISIBLE);

                        currentDayScore.setText(R.string.error_depression_score);
                    }
                });

        Volley.newRequestQueue(this).add(todaysStateRequest);
    }

    /**
     * Check the depression state of user last week.
     * @param login_id user login id
     */
    private void requestLastWeekDepressionScore(String login_id) {
        String url = BASE_URL + DEPRESSION_STATE_LAST_WEEK;

        progressBarLastWeek.setVisibility(View.VISIBLE);
        previousWeekScore.setVisibility(View.VISIBLE);

        previousWeekScore.setText(R.string.initial_message);
        JSONObject object = new JSONObject();
        try {
            object.put("UserId", login_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest todaysStateRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Got weekly response: " + response.toString());
                            String state = response.getString("status");
                            if (state.equals("Not Depressed")) {
                                progressBarLastWeek.setVisibility(View.GONE);
                                previousWeekScore.setVisibility(View.VISIBLE);

                                previousWeekScore.setText(R.string.non_depression_text_daily);
                            } else if (state.equals("Depressed")) {
                                progressBarLastWeek.setVisibility(View.GONE);
                                previousWeekScore.setVisibility(View.VISIBLE);

                                previousWeekScore.setText(R.string.depression_text_daily);
                            } else {
                                progressBarLastWeek.setVisibility(View.GONE);
                                previousWeekScore.setVisibility(View.VISIBLE);

                                previousWeekScore.setText(R.string.no_depression_score_weekly);
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "Cannot parse response");
                            progressBarLastWeek.setVisibility(View.GONE);
                            previousWeekScore.setVisibility(View.VISIBLE);

                            previousWeekScore.setText(R.string.error_depression_score_weekly);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Volley error");
                        error.printStackTrace();
                        progressBarLastWeek.setVisibility(View.GONE);
                        previousWeekScore.setVisibility(View.VISIBLE);

                        previousWeekScore.setText(R.string.error_depression_score);
                    }
                });

        Volley.newRequestQueue(this).add(todaysStateRequest);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int selectedId = item.getItemId();
        drawer.closeDrawers();
        if (selectedId == R.id.help) {
            Toast.makeText(this, "Pending.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent serviceIntent = new Intent(this, ActivityCollectionService.class);
                serviceIntent.setAction("START");
                ContextCompat.startForegroundService(this, serviceIntent);
                stopRecording.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (stopRecording.getText().toString().contains("stop")) {
                            Intent serviceIntent = new Intent(MainActivity.this, ActivityCollectionService.class);
                            serviceIntent.setAction("STOP");
                            ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
                            stopRecording.setText(R.string.start_activity_recording);
                        } else {
                            Intent serviceIntent = new Intent(MainActivity.this, ActivityCollectionService.class);
                            serviceIntent.setAction("START");
                            ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
                            stopRecording.setText(R.string.stop_activity_recording);
                        }
                    }
                });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Storage Permission is required to store your activity.");
                builder.setPositiveButton("Grant Permission.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE },
                                        1);
                                dialog.dismiss();
                            }
                        });
                builder.setCancelable(false);
                builder.create().show();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Storage Permission is required to store your activity.");
            builder.setPositiveButton("Grant Permission.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE },
                                    1);
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        }
    }
}
