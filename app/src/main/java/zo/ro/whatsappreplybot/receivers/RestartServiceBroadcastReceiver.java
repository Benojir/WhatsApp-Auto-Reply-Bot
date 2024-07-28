package zo.ro.whatsappreplybot.receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.content.ComponentName;

import zo.ro.whatsappreplybot.services.MyNotificationListenerService;

public class RestartServiceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MADARA";

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {

        if (!isNotificationServiceRunning(context)) {
            Intent serviceIntent = new Intent(context, MyNotificationListenerService.class);
            context.startService(serviceIntent);
            Log.d(TAG, "Notification service started after boot");
        } else {
            Log.d(TAG, "Notification service is already running");
        }
    }

    private boolean isNotificationServiceRunning(Context context) {
        String packageName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName componentName = ComponentName.unflattenFromString(name);
                if (componentName != null) {
                    if (TextUtils.equals(packageName, componentName.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
