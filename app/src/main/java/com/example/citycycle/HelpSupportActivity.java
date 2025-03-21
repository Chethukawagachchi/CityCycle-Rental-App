package com.example.citycycle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelpSupportActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        // Get user email from intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.help_support_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Help & Support");
        }

        // Initialize views
        expandableListView = findViewById(R.id.expandableListView);
        setupFAQData();

        ExpandableListAdapter listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(listAdapter);

        // Setup click listeners
        setupClickListeners();
    }

    private void setupFAQData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        // Adding header data
        listDataHeader.add("Booking & Reservations");
        listDataHeader.add("Payment & Cancellation");
        listDataHeader.add("Resort Facilities");
        listDataHeader.add("Check-in & Check-out");

        // Adding child data
        List<String> bookingFaqs = new ArrayList<>();
        bookingFaqs.add("How do I make a room reservation?\nYou can book rooms through our app's 'Book Rooms' section or contact our reception.");
        bookingFaqs.add("Can I modify my booking?\nYes, you can modify your booking up to 48 hours before check-in through the app.");

        List<String> paymentFaqs = new ArrayList<>();
        paymentFaqs.add("What payment methods are accepted?\nWe accept all major credit cards, debit cards, and digital wallets.");
        paymentFaqs.add("What is the cancellation policy?\nFree cancellation up to 48 hours before check-in. After that, one night's charge applies.");

        List<String> facilityFaqs = new ArrayList<>();
        facilityFaqs.add("What amenities are included?\nPool, spa, gym, restaurant, and complimentary Wi-Fi are included.");
        facilityFaqs.add("Is parking available?\nYes, we offer free valet parking for all guests.");

        List<String> checkInFaqs = new ArrayList<>();
        checkInFaqs.add("What are the check-in/out times?\nCheck-in: 2:00 PM, Check-out: 11:00 AM");
        checkInFaqs.add("Can I request early check-in?\nSubject to availability. Please contact reception in advance.");

        listDataChild.put(listDataHeader.get(0), bookingFaqs);
        listDataChild.put(listDataHeader.get(1), paymentFaqs);
        listDataChild.put(listDataHeader.get(2), facilityFaqs);
        listDataChild.put(listDataHeader.get(3), checkInFaqs);
    }

    private void setupClickListeners() {
        // Contact via email
        findViewById(R.id.emailSupportButton).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@laxvistaresort.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request - " + userEmail);
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        // Contact via phone
        findViewById(R.id.callSupportButton).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0762150482"));
            startActivity(intent);
        });


        // Live chat
        findViewById(R.id.liveChatButton).setOnClickListener(v -> {
            Toast.makeText(this, "Live chat feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}