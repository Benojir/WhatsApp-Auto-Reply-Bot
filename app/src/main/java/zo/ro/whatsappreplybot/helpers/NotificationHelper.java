package zo.ro.whatsappreplybot.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

public class NotificationHelper {

    public static boolean isNotificationListenerServiceRunning(Context context) {
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

//    ----------------------------------------------------------------------------------------------

    public static boolean isNotificationServicePermissionGranted(Context context) {
        String pkgName = context.getPackageName();

        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");

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
