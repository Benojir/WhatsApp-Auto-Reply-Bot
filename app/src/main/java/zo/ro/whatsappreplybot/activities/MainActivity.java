package zo.ro.whatsappreplybot.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import zo.ro.whatsappreplybot.databinding.ActivityMainBinding;
import zo.ro.whatsappreplybot.helpers.CustomMethods;
import zo.ro.whatsappreplybot.helpers.NotificationHelper;
import zo.ro.whatsappreplybot.services.MyNotificationListenerService;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MADARA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        ------------------------------------------------------------------------------------------

        if (!isNotificationServiceEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs access to your notifications to function. Please grant permission.")
                    .setPositiveButton("Grant", (dialog, which) -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean isBotEnabled = sharedPreferences.getBoolean("is_bot_enabled", true);

            if (isBotEnabled) {
                if (!NotificationHelper.isNotificationServiceRunning(this)) {
                    startService(new Intent(this, MyNotificationListenerService.class));
                }
            } else {
                if (NotificationHelper.isNotificationServiceRunning(this)) {
                    stopService(new Intent(this, MyNotificationListenerService.class));
                }
            }
        }

//        ------------------------------------------------------------------------------------------

        Log.d(TAG, "onCreate: " + CustomMethods.getCurrentDateTime());
        binding.settingsBtn.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

//    ----------------------------------------------------------------------------------------------

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();

        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");

        if (!TextUtils.isEmpty(flat)) {

            final String[] names = flat.split(":");

            for (String name : names) {

                final ComponentName cn = ComponentName.unflattenFromString(name);

                if (cn != null && TextUtils.equals(pkgName, cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }
}