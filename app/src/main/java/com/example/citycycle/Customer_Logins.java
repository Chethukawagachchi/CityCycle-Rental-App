package com.example.citycycle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.os.Looper;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Customer_Logins extends AppCompatActivity {

    private static final String TAG = "Customer_Logins";
    private static final String PREFS_NAME = "LoginPrefs";
    private static final int MAX_ATTEMPTS = 3;
    private static final int BLOCK_TIME = 30000; // 30 seconds
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String CURRENT_USER = "Chethukawagachchi";
    private static final String CURRENT_UTC_TIME = "2025-01-24 13:07:30";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView attemptsTextView;
    private DB_Operations dbOperations;
    private Button buttonRegister;


    private int loginAttempts = 0;
    private boolean isLoginBlocked = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastLoginAttemptTime = 0;
    private static final long MIN_TIME_BETWEEN_ATTEMPTS = 1000; // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_customer_logins);
            initializeViews();
            setupWindowInsets();
            setupClickListeners();
            setupInitialState();
            logSystemStartup();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Error initializing application", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        buttonRegister = findViewById(R.id.buttonRegister);
        attemptsTextView = findViewById(R.id.attemptsTextView);


        if (emailEditText == null || passwordEditText == null || loginButton == null ||
                buttonRegister == null || attemptsTextView == null) {
            if (emailEditText == null) Log.e(TAG, "emailEditText is null");
            if (passwordEditText == null) Log.e(TAG, "passwordEditText is null");
            if (loginButton == null) Log.e(TAG, "loginButton is null");
            if (buttonRegister == null) Log.e(TAG, "buttonRegister is null");
            if (attemptsTextView == null) Log.e(TAG, "attemptsTextView is null");

            throw new IllegalStateException("Required views not found in layout");
        }

        dbOperations = new DB_Operations(this);
    }

    private void setupWindowInsets() {
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void setupClickListeners() {
        setupLoginButton();
        setupRegisterButton();
        setupCancelButton();
        setupAdminLink();
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void setupRegisterButton() {
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "User navigating to registration screen at: " + getCurrentTimestamp());
                Intent intent = new Intent(Customer_Logins.this, Register.class);
                startActivity(intent);
            }
        });
    }

    private void setupCancelButton() {
        Button cancelButton = findViewById(R.id.CancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "User exiting application at: " + getCurrentTimestamp());
                    finishAffinity();
                }
            });
        }
    }

    private void setupAdminLink() {
        TextView adminLoginLink = findViewById(R.id.adminLoginLink);
        if (adminLoginLink != null) {
            adminLoginLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "User navigating to admin login at: " + getCurrentTimestamp());
                    Intent intent = new Intent(Customer_Logins.this, Admin_Logins.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void handleLogin() {
        if (isLoginBlocked) {
            showBlockedLoginMessage();
            return;
        }

        if (isThrottled()) {
            Toast.makeText(this, "Please wait before trying again", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (validateInputs(email, password)) {
            performLogin(email, password);
        }

        lastLoginAttemptTime = System.currentTimeMillis();
    }

    private boolean isThrottled() {
        return System.currentTimeMillis() - lastLoginAttemptTime < MIN_TIME_BETWEEN_ATTEMPTS;
    }

    private void performLogin(String email, String password) {
        try {
            checkSuspiciousActivity(email);

            if (dbOperations.loginUserWithEmail(email, password)) {
                handleSuccessfulLogin(email);
            } else {
                handleFailedLogin();
                logSecurityEvent("LOGIN_FAILED",
                        String.format("Failed login attempt for email: %s", email));
            }
        } catch (Exception e) {
            Log.e(TAG, "Login error: ", e);
            Toast.makeText(this, "Error during login process", Toast.LENGTH_SHORT).show();
            logSecurityEvent("LOGIN_ERROR",
                    String.format("Error during login: %s", e.getMessage()));
        }
    }

    private void handleSuccessfulLogin(String email) {
        trackUserSession(email);
        logLoginAttempt(email, true);
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class)
                .putExtra("USER_EMAIL", email)
                .putExtra("LOGIN_TIME", getCurrentTimestamp())
                .putExtra("IS_NEW_SESSION", true);

        startActivity(intent);
        finish();
    }

    private void handleFailedLogin() {
        loginAttempts++;
        updateAttemptsText();

        if (loginAttempts >= MAX_ATTEMPTS) {
            blockLoginFor30Seconds();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Pattern.compile(EMAIL_PATTERN).matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(this, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void blockLoginFor30Seconds() {
        isLoginBlocked = true;
        loginButton.setEnabled(false);
        attemptsTextView.setText(R.string.too_many_attempts);
        showBlockedLoginMessage();

        logSecurityEvent("LOGIN_BLOCKED",
                String.format("Too many attempts (%d). Account locked for 30 seconds.", loginAttempts));

        handler.postDelayed(() -> {
            isLoginBlocked = false;
            loginButton.setEnabled(true);
            loginAttempts = 0;
            updateAttemptsText();
            logSecurityEvent("LOGIN_UNBLOCKED", "Login attempt limit reset");
        }, BLOCK_TIME);
    }

    private void showBlockedLoginMessage() {
        Toast.makeText(this, "Too many attempts. Please wait 30 seconds.", Toast.LENGTH_SHORT).show();
    }

    private void updateAttemptsText() {
        int remainingAttempts = MAX_ATTEMPTS - loginAttempts;
        attemptsTextView.setText(String.format(Locale.getDefault(),
                "Attempts remaining: %d", remainingAttempts));
    }

    private void setupInitialState() {
        updateAttemptsText();
        loadLastLoginInfo();
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    private void trackUserSession(String email) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String loginTime = sdf.format(new Date());

        getSharedPreferences("UserSession", MODE_PRIVATE)
                .edit()
                .putString("lastLoginTime", loginTime)
                .putString("lastLoginUser", email)
                .apply();

        Log.i(TAG, String.format("User Session Started - Email: %s, Login Time (UTC): %s",
                email, loginTime));
    }

    private void loadLastLoginInfo() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String lastLoginUser = prefs.getString("lastLoginUser", "");
        String lastLoginTime = prefs.getString("lastLoginTime", "");

        if (!TextUtils.isEmpty(lastLoginUser) && !TextUtils.isEmpty(lastLoginTime)) {
            Log.i(TAG, String.format("Last login - User: %s, Time: %s", lastLoginUser, lastLoginTime));
        }
    }

    private void logSecurityEvent(String eventType, String details) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = sdf.format(new Date());

        Log.i(TAG, String.format("Security Event - Type: %s, Details: %s, Time (UTC): %s",
                eventType, details, timestamp));
    }

    private void checkSuspiciousActivity(String email) {
        SharedPreferences prefs = getSharedPreferences("SecurityPrefs", MODE_PRIVATE);
        long lastLoginAttempt = prefs.getLong("lastLoginAttempt_" + email, 0);
        long currentTime = System.currentTimeMillis();

        if (lastLoginAttempt > 0 && currentTime - lastLoginAttempt < 1000) {
            logSecurityEvent("SUSPICIOUS_ACTIVITY",
                    "Rapid login attempts detected for email: " + email);
        }

        prefs.edit().putLong("lastLoginAttempt_" + email, currentTime).apply();
    }

    private void logSystemStartup() {
        Log.i(TAG, String.format("System startup - Current User: %s, Time (UTC): %s",
                CURRENT_USER, CURRENT_UTC_TIME));
    }

    private void logLoginAttempt(String email, boolean success) {
        String timestamp = getCurrentTimestamp();
        Log.i(TAG, String.format("Login attempt: Email=%s, Success=%b, Time=%s",
                email, success, timestamp));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        Log.i(TAG, "Application shutting down at: " + getCurrentTimestamp());
    }
}