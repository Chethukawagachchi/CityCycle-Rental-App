package com.example.citycycle;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewDiscountActivity extends AppCompatActivity {
    private static final String CURRENT_UTC_TIME = "2025-03-03 04:05:00";
    private static final String CURRENT_USER = "Chethukawagachchi";

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TextView txtNoDiscounts;
    private DB_Operations dbOperations;
    private ViewDiscountAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_discount);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadActiveDiscounts();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewDiscounts);
        txtNoDiscounts = findViewById(R.id.txtNoDiscounts);
        dbOperations = new DB_Operations(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Available Discounts");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ViewDiscountAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadActiveDiscounts() {
        List<Discount> allDiscounts = dbOperations.getAllDiscounts();
        List<Discount> activeDiscounts = new ArrayList<>();

        // Filter active discounts
        for (Discount discount : allDiscounts) {
            if (isDiscountActive(discount)) {
                activeDiscounts.add(discount);
            }
        }

        if (activeDiscounts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            txtNoDiscounts.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            txtNoDiscounts.setVisibility(View.GONE);
            adapter.setDiscounts(activeDiscounts);
        }
    }

    private boolean isDiscountActive(Discount discount) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date validUntil = sdf.parse(discount.getValidUntil());
            Date currentDate = sdf.parse(CURRENT_UTC_TIME.split(" ")[0]);
            return validUntil != null && validUntil.after(currentDate);
        } catch (ParseException e) {
            return false;
        }
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