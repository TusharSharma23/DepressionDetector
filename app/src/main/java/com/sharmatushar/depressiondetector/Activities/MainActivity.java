package com.sharmatushar.depressiondetector.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
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

import com.google.android.material.navigation.NavigationView;
import com.sharmatushar.depressiondetector.R;
import com.sharmatushar.depressiondetector.Services.ActivityCollectionService;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer;
    private TextView currentDayScore, previousDaysScore, stopRecording;
    private ImageView scoreDescriptor;

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
        previousDaysScore = findViewById(R.id.overall_depression_score);
        scoreDescriptor = findViewById(R.id.score_depict);

        //setting values here
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        Objects.requireNonNull(getSupportActionBar()).hide();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.status_bar_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

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

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int selectedId = item.getItemId();
        drawer.closeDrawers();
        switch (selectedId) {
            case R.id.help:
                Toast.makeText(this, "Pending.", Toast.LENGTH_SHORT).show();
                break;
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
