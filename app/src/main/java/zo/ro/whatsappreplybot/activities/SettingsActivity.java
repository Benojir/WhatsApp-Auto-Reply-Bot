package zo.ro.whatsappreplybot.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import zo.ro.whatsappreplybot.R;
import zo.ro.whatsappreplybot.helpers.CustomMethods;
import zo.ro.whatsappreplybot.helpers.NotificationHelper;
import zo.ro.whatsappreplybot.others.InputFilterMinMax;
import zo.ro.whatsappreplybot.services.MyNotificationListenerService;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

//        ------------------------------------------------------------------------------------------

        if (CustomMethods.isNotificationServiceEnabled(this)){

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
        } else {
            startActivity(new Intent(this, SetupActivity.class));
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1000);
        }

//        ------------------------------------------------------------------------------------------

//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference editTextPreference = findPreference("max_reply");
            if (editTextPreference != null) {
                editTextPreference.setOnBindEditTextListener(editText -> {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setFilters(new InputFilter[]{new InputFilterMinMax(1, 999999999)});
                });
            }
        }
    }
}