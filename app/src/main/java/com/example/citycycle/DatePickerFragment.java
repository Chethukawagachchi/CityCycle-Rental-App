package com.example.citycycle;



import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener listener;
    private static final String CURRENT_UTC_TIME = "2025-03-03 02:18:03";

    public void setOnDateSelectedListener(OnDateSelectedListener onDateSelectedListener) {
        this.listener = (view, year, month, day) ->
                onDateSelectedListener.onDateSelected(year, month, day);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), listener, year, month, day);

        // Set minimum date to current date
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        return dialog;
    }

    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int day);
    }
}