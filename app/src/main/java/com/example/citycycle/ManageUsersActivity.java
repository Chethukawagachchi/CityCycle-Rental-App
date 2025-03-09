package com.example.citycycle;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ManageUsersActivity extends AppCompatActivity {
    EditText txtUsername, txtMobile, txtAddress, txtEmail, txtPassword;
    DB_Operations dbOperations;
    Button btnClearFields, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        dbOperations = new DB_Operations(this);

        txtUsername = findViewById(R.id.txtUsername);
        txtMobile = findViewById(R.id.txtMobile);
        txtAddress = findViewById(R.id.txtAddress);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnClearFields = findViewById(R.id.btnClearFields);
        btnBack = findViewById(R.id.btnBack);

        btnClearFields.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back(v);
            }
        });
    }

    public void addUser(View view) {
        if (validateFields()) {
            User user = new User(
                    txtUsername.getText().toString(),
                    Integer.parseInt(txtMobile.getText().toString()),
                    txtAddress.getText().toString(),
                    txtEmail.getText().toString(),
                    txtPassword.getText().toString()
            );

            try {
                dbOperations.createUser(user);
                Toast.makeText(this, "User Added Successfully", Toast.LENGTH_SHORT).show();
                clearFields();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Email Already Added User", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void searchUser(View view) {
        if (!txtEmail.getText().toString().isEmpty()) {
            User user = dbOperations.getUserByEmail(txtEmail.getText().toString());

            if (user != null) {
                txtUsername.setText(user.getUsername());
                txtMobile.setText(String.valueOf(user.getMobile()));
                txtAddress.setText(user.getAddress());
                txtPassword.setText(user.getPassword());
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter an Email", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateUser(View view) {
        if (validateFields()) {
            User user = new User();
            user.setUsername(txtUsername.getText().toString());
            user.setMobile(Integer.parseInt(txtMobile.getText().toString()));
            user.setAddress(txtAddress.getText().toString());
            user.setEmail(txtEmail.getText().toString());
            user.setPassword(txtPassword.getText().toString());

            try {
                dbOperations.updateUser(user);
                Toast.makeText(this, "User Updated Successfully", Toast.LENGTH_SHORT).show();
                clearFields();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error Updating User", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void deleteUser(View view) {
        if (!txtEmail.getText().toString().isEmpty()) {
            try {
                dbOperations.deleteUser(txtEmail.getText().toString());
                Toast.makeText(this, "User Deleted Successfully", Toast.LENGTH_SHORT).show();
                clearFields();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error Deleting User", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter an Email", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearFields() {
        txtUsername.setText(null);
        txtMobile.setText(null);
        txtAddress.setText(null);
        txtEmail.setText(null);
        txtPassword.setText(null);
    }

    private boolean validateFields() {
        String username = txtUsername.getText().toString().trim();
        String mobile = txtMobile.getText().toString().trim();
        String address = txtAddress.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a Username", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mobile.isEmpty()) {
            Toast.makeText(this, "Please enter a Mobile number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mobile.length() != 10 || !mobile.matches("\\d+")) {
            Toast.makeText(this, "Mobile number must be exactly 10 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter an Address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an Email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!email.endsWith("@gmail.com")) {
            Toast.makeText(this, "Email must end with @gmail.com", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a Password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void back(View view) {
        Intent intent = new Intent(ManageUsersActivity.this, AdminDashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
