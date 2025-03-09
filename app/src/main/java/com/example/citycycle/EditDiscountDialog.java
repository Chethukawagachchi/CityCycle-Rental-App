package com.example.citycycle;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditDiscountDialog extends DialogFragment {
    private static final String CURRENT_UTC_TIME = "2025-03-03 02:21:24";
    private static final String CURRENT_USER = "Chethukawagachchi";

    private Discount discount;
    private OnDiscountEditedListener listener;
    private TextInputEditText edtDiscountCode;
    private TextInputEditText edtDiscountPercentage;
    private TextInputEditText edtValidUntil;

    public EditDiscountDialog(Discount discount) {
        this.discount = discount;
    }

    public interface OnDiscountEditedListener {
        void onDiscountEdited(Discount discount);
    }

    public void setOnDiscountEditedListener(OnDiscountEditedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_edit_discount_dialog, null);

        initializeViews(view);
        populateFields();

        builder.setView(view)
                .setTitle("Edit Discount")
                .setPositiveButton("Save", (dialog, id) -> {
                    if (validateInputs()) {
                        saveDiscount();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());

        return builder.create();
    }

    private void initializeViews(View view) {
        edtDiscountCode = view.findViewById(R.id.edtDiscountCode);
        edtDiscountPercentage = view.findViewById(R.id.edtDiscountPercentage);
        edtValidUntil = view.findViewById(R.id.edtValidUntil);

        edtValidUntil.setOnClickListener(v -> showDatePicker());
    }

    private void populateFields() {
        edtDiscountCode.setText(discount.getCode());
        edtDiscountPercentage.setText(String.valueOf(discount.getPercentage()));
        edtValidUntil.setText(discount.getValidUntil());
    }

    private void showDatePicker() {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setOnDateSelectedListener((year, month, day) -> {
            String selectedDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day);
            edtValidUntil.setText(selectedDate);
        });
        datePickerFragment.show(getParentFragmentManager(), "datePicker");
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String code = edtDiscountCode.getText().toString().trim();
        String percentageStr = edtDiscountPercentage.getText().toString().trim();
        String validUntil = edtValidUntil.getText().toString().trim();

        if (code.isEmpty()) {
            edtDiscountCode.setError("Please enter discount code");
            isValid = false;
        }

        if (percentageStr.isEmpty()) {
            edtDiscountPercentage.setError("Please enter percentage");
            isValid = false;
        } else {
            try {
                int percentage = Integer.parseInt(percentageStr);
                if (percentage <= 0 || percentage > 100) {
                    edtDiscountPercentage.setError("Percentage must be between 1 and 100");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                edtDiscountPercentage.setError("Invalid percentage");
                isValid = false;
            }
        }

        if (validUntil.isEmpty()) {
            edtValidUntil.setError("Please select validity date");
            isValid = false;
        }

        return isValid;
    }

    private void saveDiscount() {
        try {
            String code = edtDiscountCode.getText().toString().trim();
            int percentage = Integer.parseInt(edtDiscountPercentage.getText().toString().trim());
            String validUntil = edtValidUntil.getText().toString().trim();

            Discount updatedDiscount = new Discount(
                    discount.getId(),
                    code,
                    percentage,
                    validUntil,
                    CURRENT_UTC_TIME,
                    CURRENT_USER
            );

            if (listener != null) {
                listener.onDiscountEdited(updatedDiscount);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error updating discount", Toast.LENGTH_SHORT).show();
        }
    }
}