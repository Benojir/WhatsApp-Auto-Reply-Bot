package zo.ro.whatsappreplybot.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import zo.ro.whatsappreplybot.databinding.ActivitySetupBinding;
import zo.ro.whatsappreplybot.helpers.CustomMethods;
import zo.ro.whatsappreplybot.helpers.NotificationHelper;
import zo.ro.whatsappreplybot.services.MyNotificationListenerService;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySetupBinding binding = ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        ------------------------------------------------------------------------------------------

        if (!CustomMethods.isNotificationServiceEnabled(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs access to your notifications to function. Please grant permission.")
                    .setPositiveButton("Grant", (dialog, which) -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }

//        ------------------------------------------------------------------------------------------

        String TAG = "MADARA";
        Log.d(TAG, "onCreate: " + CustomMethods.getCurrentDateTime());
        binding.settingsBtn.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

//    ----------------------------------------------------------------------------------------------


}