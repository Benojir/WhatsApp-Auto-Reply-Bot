package zo.ro.whatsappreplybot.receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import zo.ro.whatsappreplybot.helpers.NotificationHelper;
import zo.ro.whatsappreplybot.services.MyNotificationListenerService;

public class RestartServiceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MADARA";

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPreferences.getBoolean("is_bot_enabled", true)) {
            if (!NotificationHelper.isNotificationServiceRunning(context)) {
                Intent serviceIntent = new Intent(context, MyNotificationListenerService.class);
                context.startService(serviceIntent);
                Log.d(TAG, "Notification service started after boot");
            } else {
                Log.d(TAG, "Notification service is already running");
            }
        }

    }
}
