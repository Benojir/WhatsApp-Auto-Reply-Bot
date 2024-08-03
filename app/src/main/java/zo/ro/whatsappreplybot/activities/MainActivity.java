package zo.ro.whatsappreplybot.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;

import zo.ro.whatsappreplybot.R;
import zo.ro.whatsappreplybot.databinding.ActivityMainBinding;
import zo.ro.whatsappreplybot.helpers.NotificationHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isSettingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (NotificationHelper.isNotificationServicePermissionGranted(this)) {
            setSettingsButton();
        }

        binding.permissionAndSettingsBtn.setOnClickListener(v -> {
            if (isSettingsButton) {
                startActivity(new Intent(this, BotSettingsActivity.class));
            } else {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        });
    }

    //--------------------------------------------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();

        if (NotificationHelper.isNotificationServicePermissionGranted(this)) {
            setSettingsButton();
        }
    }

//    ----------------------------------------------------------------------------------------------

    private void setSettingsButton() {
        isSettingsButton = true;
        binding.permissionAndSettingsBtn.setText(R.string.bot_settings);
        binding.permissionAndSettingsBtn.setAllCaps(true);
        binding.permissionAndSettingsBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings_24, 0, 0, 0);
        int tintColor = ContextCompat.getColor(this, R.color.white);
        TextViewCompat.setCompoundDrawableTintList(binding.permissionAndSettingsBtn, ColorStateList.valueOf(tintColor));
        ColorStateList backgroundTint = ColorStateList.valueOf(getColor(R.color.teal));
        ViewCompat.setBackgroundTintList(binding.permissionAndSettingsBtn, backgroundTint);
        binding.shortInfoTV.setText(getString(R.string.manage_bot_settings));
    }
}