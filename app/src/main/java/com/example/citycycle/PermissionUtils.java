package com.example.citycycle;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

public class PermissionUtils {

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86");
    }

    public static void handleFeature(Context context, String feature) {
        switch (feature) {
            case "sms":
                if (isEmulator()) {
                    // Show emulator message
                    Toast.makeText(context,
                            "SMS feature is not available on emulator. This would send an SMS on a real device.",
                            Toast.LENGTH_LONG).show();
                } else {
                    // Use SMSUtils for real device
                    // Your SMS sending code here
                }
                break;

            case "vibrate":
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(500);
                    }
                }
                break;

            case "storage":
                if (isEmulator()) {
                    // Use emulator storage path
                    // /storage/emulated/0/
                } else {
                    // Use real device storage path
                    // Environment.getExternalStorageDirectory()
                }
                break;
        }
    }
}