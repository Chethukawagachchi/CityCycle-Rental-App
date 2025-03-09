package com.example.citycycle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ManageBikesActivity extends AppCompatActivity {
    EditText txtBikeID, txtPrice, txtDescription;
    // Add spinner declaration
    private Spinner spinnerLocation;
    private EditText txtLocation;
    Spinner spinnerBikeType, spinnerAvailability;
    ImageView imgBike;
    byte[] imageByte;

    DB_Operations dbOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_bikes);

        dbOperations = new DB_Operations(this);

        // Initialize views
        txtBikeID = findViewById(R.id.txtBikeID);
        spinnerBikeType = findViewById(R.id.spinnerBikeType);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        txtLocation = findViewById(R.id.txtLocation);
        imgBike = findViewById(R.id.imgBike);
        spinnerAvailability = findViewById(R.id.spinnerAvailability);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Set up bike type spinner
        ArrayAdapter<CharSequence> bikeTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.bike_types, android.R.layout.simple_spinner_item);
        bikeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBikeType.setAdapter(bikeTypeAdapter);

        // Set up location spinner
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(this,
                R.array.predefined_locations, android.R.layout.simple_spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);

        // Add listener for location spinner
        spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLocation = parent.getItemAtPosition(position).toString();
                if (selectedLocation.equals("Custom Location")) {
                    txtLocation.setVisibility(View.VISIBLE);
                } else {
                    txtLocation.setVisibility(View.GONE);
                    txtLocation.setText(""); // Clear the text field
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txtLocation.setVisibility(View.GONE);
            }
        });

        // Availability Spinner Setup
        ArrayAdapter<CharSequence> availabilityAdapter = ArrayAdapter.createFromResource(this,
                R.array.availability_options, android.R.layout.simple_spinner_item);
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailability.setAdapter(availabilityAdapter);

        //Initially hide the custom text field
        txtLocation.setVisibility(View.GONE);
    }

    public void selectImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputX", 170);
        intent.putExtra("return-data", true);
        startActivityForResult(Intent.createChooser(intent, "Select Bike Image"), 111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
                imageByte = byteArrayOutputStream.toByteArray();
                imgBike.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Update the insertBike method
    public void insertBike(View view) {
        if (validateFields()) {
            Bike bike = new Bike();
            bike.setBikeID(Integer.parseInt(txtBikeID.getText().toString()));
            bike.setBikeType(spinnerBikeType.getSelectedItem().toString());
            bike.setPricePerHour(Double.parseDouble(txtPrice.getText().toString()));
            bike.setDescription(txtDescription.getText().toString());

            // Handle location based on spinner selection
            String selectedLocation = spinnerLocation.getSelectedItem().toString();
            if (selectedLocation.equals("Custom Location")) {
                bike.setLocation(txtLocation.getText().toString());
            } else {
                bike.setLocation(selectedLocation);
            }

            bike.setImage(imageByte);
            bike.setAvailability(spinnerAvailability.getSelectedItem().toString());

            try {
                dbOperations.addBike(bike);
                Toast.makeText(this, "Bike Record Inserted", Toast.LENGTH_SHORT).show();
                clearFields(null);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Bike ID Already Added", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Update the searchBikeById method
    public void searchBikeById(View view) {
        if (!txtBikeID.getText().toString().isEmpty()) {
            int bikeID = Integer.parseInt(txtBikeID.getText().toString());
            Bike bike = dbOperations.getBikeById(bikeID);

            if (bike != null) {
                // Set bike type spinner
                ArrayAdapter bikeTypeAdapter = (ArrayAdapter) spinnerBikeType.getAdapter();
                int bikeTypePosition = bikeTypeAdapter.getPosition(bike.getBikeType());
                spinnerBikeType.setSelection(bikeTypePosition);

                txtPrice.setText(String.valueOf(bike.getPricePerHour()));
                txtDescription.setText(bike.getDescription());

                // Handle location setting
                ArrayAdapter locationAdapter = (ArrayAdapter) spinnerLocation.getAdapter();
                int locationPosition = locationAdapter.getPosition(bike.getLocation());
                if (locationPosition != -1) {
                    // If location is in predefined list
                    spinnerLocation.setSelection(locationPosition);
                    txtLocation.setVisibility(View.GONE);
                } else {
                    // If location is custom
                    spinnerLocation.setSelection(locationAdapter.getPosition("Custom Location"));
                    txtLocation.setVisibility(View.VISIBLE);
                    txtLocation.setText(bike.getLocation());
                }

                imgBike.setImageBitmap(BitmapFactory.decodeByteArray(bike.getImage(), 0, bike.getImage().length));

                ArrayAdapter availabilityAdapter = (ArrayAdapter) spinnerAvailability.getAdapter();
                int availabilityPosition = availabilityAdapter.getPosition(bike.getAvailability());
                spinnerAvailability.setSelection(availabilityPosition);

            } else {
                Toast.makeText(this, "Bike not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a Bike ID", Toast.LENGTH_SHORT).show();
        }
    }

    // Update the updateBike method
    public void updateBike(View view) {
        if (validateFields()) {
            Bike bike = new Bike();
            bike.setBikeID(Integer.parseInt(txtBikeID.getText().toString()));
            bike.setBikeType(spinnerBikeType.getSelectedItem().toString());
            bike.setPricePerHour(Double.parseDouble(txtPrice.getText().toString()));
            bike.setDescription(txtDescription.getText().toString());

            // Handle location based on spinner selection
            String selectedLocation = spinnerLocation.getSelectedItem().toString();
            if (selectedLocation.equals("Custom Location")) {
                bike.setLocation(txtLocation.getText().toString());
            } else {
                bike.setLocation(selectedLocation);
            }

            bike.setImage(imageByte);
            bike.setAvailability(spinnerAvailability.getSelectedItem().toString());

            try {
                dbOperations.updateBike(bike);
                Toast.makeText(this, "Bike Record Updated", Toast.LENGTH_SHORT).show();
                clearFields(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Update the clearFields method
    public void clearFields(View view) {
        txtBikeID.setText(null);
        spinnerBikeType.setSelection(0);
        txtPrice.setText(null);
        txtDescription.setText(null);
        spinnerLocation.setSelection(0);
        txtLocation.setText(null);
        txtLocation.setVisibility(View.GONE);
        imgBike.setImageDrawable(null);
        imageByte = null;
        spinnerAvailability.setSelection(0);
    }

    // Update the validateFields method
    private boolean validateFields() {
        if (txtBikeID.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a Bike ID", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtPrice.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a Price", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtDescription.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a Description", Toast.LENGTH_SHORT).show();
            return false;
        }

        String selectedLocation = spinnerLocation.getSelectedItem().toString();
        if (selectedLocation.equals("Select Location")) {
            Toast.makeText(this, "Please select a Location", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedLocation.equals("Custom Location") && txtLocation.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a Custom Location", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (imageByte == null) {
            Toast.makeText(this, "Please select an Image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}