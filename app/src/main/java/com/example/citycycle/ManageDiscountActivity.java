package com.example.citycycle;



import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageDiscountActivity extends AppCompatActivity {
    private static final String TAG = "ManageDiscountActivity";
    private static final String CURRENT_USER = "Chethukawagachchi";
    private static final String CURRENT_UTC_TIME = "2025-03-03 02:05:33";

    private Toolbar toolbar;
    private TextInputEditText edtDiscountCode;
    private TextInputEditText edtDiscountPercentage;
    private TextInputEditText edtValidUntil;
    private MaterialButton btnAddDiscount;
    private RecyclerView recyclerDiscounts;
    private DB_Operations dbOperations;
    private DiscountAdapter discountAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_discount);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadDiscounts();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        edtDiscountCode = findViewById(R.id.edtDiscountCode);
        edtDiscountPercentage = findViewById(R.id.edtDiscountPercentage);
        edtValidUntil = findViewById(R.id.edtValidUntil);
        btnAddDiscount = findViewById(R.id.btnAddDiscount);
        recyclerDiscounts = findViewById(R.id.recyclerDiscounts);
        dbOperations = new DB_Operations(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Discounts");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        recyclerDiscounts.setLayoutManager(new LinearLayoutManager(this));
        discountAdapter = new DiscountAdapter(discount -> {
            // Handle discount item click - maybe edit or delete
            showDiscountOptions(discount);
        });
        recyclerDiscounts.setAdapter(discountAdapter);
    }

    private void setupClickListeners() {
        btnAddDiscount.setOnClickListener(v -> addNewDiscount());
        edtValidUntil.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setOnDateSelectedListener((year, month, day) -> {
            String selectedDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day);
            edtValidUntil.setText(selectedDate);
        });
        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void addNewDiscount() {
        if (validateInputs()) {
            String code = edtDiscountCode.getText().toString().trim();
            int percentage = Integer.parseInt(edtDiscountPercentage.getText().toString());
            String validUntil = edtValidUntil.getText().toString();

            Discount discount = new Discount(
                    0, // ID will be set by database
                    code,
                    percentage,
                    validUntil,
                    CURRENT_UTC_TIME,
                    CURRENT_USER
            );

            long result = dbOperations.addDiscount(discount);
            if (result > 0) {
                Toast.makeText(this, "Discount added successfully", Toast.LENGTH_SHORT).show();
                clearInputs();
                loadDiscounts();
            } else {
                Toast.makeText(this, "Failed to add discount", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (TextUtils.isEmpty(edtDiscountCode.getText())) {
            edtDiscountCode.setError("Please enter discount code");
            isValid = false;
        }

        if (TextUtils.isEmpty(edtDiscountPercentage.getText())) {
            edtDiscountPercentage.setError("Please enter discount percentage");
            isValid = false;
        } else {
            try {
                int percentage = Integer.parseInt(edtDiscountPercentage.getText().toString());
                if (percentage <= 0 || percentage > 100) {
                    edtDiscountPercentage.setError("Percentage must be between 1 and 100");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                edtDiscountPercentage.setError("Invalid percentage");
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(edtValidUntil.getText())) {
            edtValidUntil.setError("Please select validity date");
            isValid = false;
        }

        return isValid;
    }

    private void clearInputs() {
        edtDiscountCode.setText("");
        edtDiscountPercentage.setText("");
        edtValidUntil.setText("");
    }

    private void loadDiscounts() {
        List<Discount> discounts = dbOperations.getAllDiscounts();
        discountAdapter.setDiscounts(discounts);
    }

    private void showDiscountOptions(Discount discount) {
        new AlertDialog.Builder(this)
                .setTitle("Discount Options")
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        editDiscount(discount);
                    } else {
                        deleteDiscount(discount);
                    }
                })
                .show();
    }

    private void editDiscount(Discount discount) {
        // Show edit dialog
        EditDiscountDialog dialog = new EditDiscountDialog(discount);
        dialog.setOnDiscountEditedListener(editedDiscount -> {
            if (dbOperations.updateDiscount(editedDiscount)) {
                Toast.makeText(this, "Discount updated successfully", Toast.LENGTH_SHORT).show();
                loadDiscounts();
            } else {
                Toast.makeText(this, "Failed to update discount", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getSupportFragmentManager(), "EditDiscount");
    }

    private void deleteDiscount(Discount discount) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Discount")
                .setMessage("Are you sure you want to delete this discount?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbOperations.deleteDiscount(discount.getId())) {
                        Toast.makeText(this, "Discount deleted successfully", Toast.LENGTH_SHORT).show();
                        loadDiscounts();
                    } else {
                        Toast.makeText(this, "Failed to delete discount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}