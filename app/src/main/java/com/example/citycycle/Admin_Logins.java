package com.example.citycycle;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Admin_Logins extends AppCompatActivity {

    private EditText txtUsername, txtPassword;
    private TextView lblAttempts;
    private Button btnLogin, btnCancel;

    private int loginAttempts = 0;
    private final int MAX_ATTEMPTS = 3;
    private final int TIME_OUT_TIME = 30000;  // 30 seconds timeout for login attempts

    // Hardcoded admin credentials
    private final String HOTEL_ADMIN_USERNAME = "admin";
    private final String HOTEL_ADMIN_PASSWORD = "123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_logins);

        // Bind views
        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        lblAttempts = findViewById(R.id.lblAttempts);
        btnLogin = findViewById(R.id.btnLogin);
        btnCancel = findViewById(R.id.btnCancel);

        // Set initial attempts text
        lblAttempts.setText("Attempts: " + loginAttempts);
    }

    // Method to handle login process
    public void processLogin(View view) {
        String username = txtUsername.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        // Check if username or password fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username or Password cannot be Empty", Toast.LENGTH_SHORT).show();
        } else {
            // Check if the entered credentials match the hardcoded values
            if (HOTEL_ADMIN_USERNAME.equals(username) && HOTEL_ADMIN_PASSWORD.equals(password)) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                // Redirect to admin activity if login is successful
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();  // Close the login screen
            } else {
                loginAttempts++;
                lblAttempts.setText("Attempts: " + loginAttempts);

                // Check if the maximum login attempts have been reached
                if (loginAttempts >= MAX_ATTEMPTS) {
                    btnLogin.setEnabled(false);
                    Toast.makeText(this, "Too many Attempts. Try again in 30 seconds", Toast.LENGTH_SHORT).show();

                    // Re-enable the login button after the timeout period
                    new Handler().postDelayed(() -> {
                        btnLogin.setEnabled(true);
                        loginAttempts = 0;
                        lblAttempts.setText("Attempts: " + loginAttempts);
                    }, TIME_OUT_TIME);
                } else {
                    Toast.makeText(this, "Login Failed. Attempts " + loginAttempts + " of " + MAX_ATTEMPTS, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Method to handle cancel button click
    public void cancel(View view) {
        // Navigate back to the customer login screen
        Intent intent = new Intent(this, Customer_Logins.class);
        startActivity(intent);
        finish();  // Close the current admin login screen
    }
}