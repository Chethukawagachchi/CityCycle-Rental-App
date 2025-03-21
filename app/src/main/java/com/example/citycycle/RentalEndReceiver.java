package com.example.citycycle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RentalEndReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int bikeId = intent.getIntExtra("bikeId", -1);

        if (bikeId != -1) {
            // Update bike availability in database
            DB_Operations dbOperations = new DB_Operations(context);
            dbOperations.updateBikeAvailability(bikeId, "Available");

            // Send broadcast to update UI if the app is open
            Intent updateIntent = new Intent("BIKE_AVAILABILITY_UPDATED");
            updateIntent.putExtra("bikeId", bikeId);
            updateIntent.putExtra("availability", "Available");
            context.sendBroadcast(updateIntent);
        }
    }
}