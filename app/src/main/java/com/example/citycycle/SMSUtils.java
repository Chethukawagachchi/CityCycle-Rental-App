package com.example.citycycle;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SMSUtils {
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;

    public static void sendSMS(Context context, String phoneNumber, String message) {
        // Check if we have SMS permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission if we don't have it
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
            return;
        }

        try {
            // Check if device supports SMS
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Device doesn't support SMS
                Toast.makeText(context,
                        "This device doesn't support SMS functionality",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(context,
                    "SMS failed to send. Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Call this method in your activity's onRequestPermissionsResult
    public static void handlePermissionResult(Context context, int requestCode,
                                              String[] permissions, int[] grantResults) {
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context,
                        "SMS permission granted",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context,
                        "SMS permission denied. Cannot send SMS notifications.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}