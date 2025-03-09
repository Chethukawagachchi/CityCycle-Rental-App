package com.example.citycycle;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

public class Register extends AppCompatActivity {

    private EditText usernameEditText, mobileEditText, addressEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private DB_Operations dbOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // Initialize views
        usernameEditText = findViewById(R.id.username);
        mobileEditText = findViewById(R.id.etMobile);
        addressEditText = findViewById(R.id.etAddress);
        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword); // Updated to match XML ID
        registerButton = findViewById(R.id.registerButton);

        // Check for null views
        if (usernameEditText == null || mobileEditText == null || addressEditText == null ||
                emailEditText == null || passwordEditText == null || confirmPasswordEditText == null ||
                registerButton == null) {
            Log.e("RegisterActivity", "One or more views are null. Check IDs in the layout file.");
            throw new IllegalStateException("View initialization failed. Check layout file and IDs.");
        }

        // Initialize database operations
        dbOperations = new DB_Operations(this);

        // Create notification channel for devices running Android Oreo (API level 26) or higher
        createNotificationChannel();

        // Register button click listener
        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String mobile = mobileEditText.getText().toString();
            String address = addressEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            if (validateInputs(username, mobile, address, email, password, confirmPassword)) {
                User newUser = new User(username, Integer.parseInt(mobile), address, email, password);
                try {
                    dbOperations.createUser(newUser);
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    sendNotification();
                    startActivity(new Intent(Register.this, Customer_Logins.class));
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Create notification channel for devices running Android Oreo (API level 26) or higher
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Registration Channel";
            String description = "Channel for registration notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("registration_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "registration_channel")
                .setSmallIcon(R.drawable.menu_icon) // Use your desired icon here
                .setContentTitle("Registration Successful")
                .setContentText("Welcome to LuxeVista Resort! You have successfully registered.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    private boolean validateInputs(String username, String mobile, String address, String email, String password, String confirmPassword) {
        if (username.isEmpty() || mobile.isEmpty() || address.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mobile.length() != 10) {
            Toast.makeText(this, "Mobile number must be exactly 10 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
