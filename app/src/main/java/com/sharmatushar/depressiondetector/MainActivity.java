package com.sharmatushar.depressiondetector;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer;

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

        //setting values here
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        Objects.requireNonNull(getSupportActionBar()).hide();

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

}
