package com.example.citycycle;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private String userEmail;
    private TextView currentTimeText;
    private static final String CURRENT_TIME = "2025-02-27 11:40:22";
    private static final String CURRENT_USER = "Chethukawagachchi";
    private VideoView videoView;
    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userEmail = getIntent().getStringExtra("USER_EMAIL");

        setupToolbar();
        setupDrawerNavigation();
        setupBottomNavigation();
        setupTimeDisplay();
        setupWelcomeAnimation();
        setupVideo();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("CityCycle");
        }
    }

    private void setupTimeDisplay() {
        currentTimeText = findViewById(R.id.currentTimeText);
        if (currentTimeText != null) {
            currentTimeText.setText(String.format("Current Time (UTC): %s\nUser: %s",
                    CURRENT_TIME, CURRENT_USER));
        }
    }

    private void setupDrawerNavigation() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (userEmail != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView headerEmail = headerView.findViewById(R.id.header_email);
            headerEmail.setText(userEmail);
        }

        navigationView.setNavigationItemSelectedListener(this::handleDrawerNavigation);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.nav_bikes) {
                intent = new Intent(this, ViewBikesActivity.class);
            } else if (itemId == R.id.nav_discounts) {
                intent = new Intent(this, ViewDiscountActivity.class);
            } else if (itemId == R.id.nav_locations) {
                intent = new Intent(this, LocationsActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private boolean handleDrawerNavigation(MenuItem item) {
        int itemId = item.getItemId();
        Intent intent = null;

        if (itemId == R.id.nav_bikes) {
            intent = new Intent(this, ViewBikesActivity.class);
        } else if (itemId == R.id.nav_active_rentals) {
            intent = new Intent(this, RentalsActivity.class);
        } else if (itemId == R.id.nav_rental_history) {
            intent = new Intent(this, RentalHistoryActivity.class);
        } else if (itemId == R.id.nav_locations) {
            intent = new Intent(this, LocationsActivity.class);
        }
        else if (itemId == R.id.nav_discounts) {
            intent = new Intent(this, ViewDiscountActivity.class);
        }
        else if (itemId == R.id.nav_profile) {
            intent = new Intent(this, ProfileActivity.class);
        } else if (itemId == R.id.nav_notifications) {
            intent = new Intent(this, NotificationActivity.class);
        } else if (itemId == R.id.nav_help) {
            intent = new Intent(this, HelpSupportActivity.class);
        } else if (itemId == R.id.nav_preferences) {
            intent = new Intent(this, PreferencesActivity.class);
        } else if (itemId == R.id.nav_about) {
            intent = new Intent(this, AboutActivity.class);
        } else if (itemId == R.id.nav_logout) {
            showLogoutDialog();
        }

        if (intent != null) {
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, Customer_Logins.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void setupWelcomeAnimation() {
        // welcomeContainer = findViewById(R.id.welcomeContainer);
        //if (welcomeContainer != null) {
        // Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        //welcomeContainer.setVisibility(View.VISIBLE);
        //welcomeContainer.startAnimation(slideDown);
        //}
    }



    private void setupVideo() {
        videoView = findViewById(R.id.videoView);
        if (videoView == null) {
            Log.e("MainActivity", "VideoView is null.  Check your layout.");
            return;
        }

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.simale_vedio);
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            // Restore position if we have one
            if (currentPosition > 0) {
                videoView.seekTo(currentPosition);
            }
            videoView.start();
            Log.d("MainActivity", "Video playback started."); // Log when playback starts
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            Log.e("MainActivity", "Video playback error: what=" + what + ", extra=" + extra);
            return true; // Indicate that we handled the error
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save the current position
        if (videoView != null) {
            currentPosition = videoView.getCurrentPosition();
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume video playback
        if (videoView != null) {
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the VideoView
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

}