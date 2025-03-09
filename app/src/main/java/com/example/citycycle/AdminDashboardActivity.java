package com.example.citycycle;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class AdminDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_icon);
        }

        navigationView.setNavigationItemSelectedListener(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_manage_users) {
            Toast.makeText(this, "Manage Users clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ManageUsersActivity.class));
        } else if (item.getItemId() == R.id.nav_view_users) {
            Toast.makeText(this, "View Users clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminUsersActivity.class));
        } else if (item.getItemId() == R.id.nav_manage_bikes) {
            Toast.makeText(this, "Manage Bikes clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ManageBikesActivity.class));
        } else if (item.getItemId() == R.id.nav_view_bikes) {
            Toast.makeText(this, "View Bikes clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminViewBikesActivity.class));
        } else if (item.getItemId() == R.id.nav_view_rentals) {
            Toast.makeText(this, "View Rentals clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, RentalsActivity.class));
        } else if (item.getItemId() == R.id.nav_manage_locations) {
            Toast.makeText(this, "Manage Locations clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ManageLocationsActivity.class));
        } else if (item.getItemId() == R.id.nav_manage_discounts) {
            Toast.makeText(this, "Manage Discount clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ManageDiscountActivity.class));
        } else if (item.getItemId() == R.id.nav_reports) {
            Toast.makeText(this, "Reports clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminReportsActivity.class));
        } else if (item.getItemId() == R.id.nav_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout Confirmation")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                       // Intent intent = new Intent(this, AdminLoginActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                       // startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}