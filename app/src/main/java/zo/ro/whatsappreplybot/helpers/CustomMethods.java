package zo.ro.whatsappreplybot.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CustomMethods {

    public static String getCurrentDateTime(){
        Calendar calendar = Calendar.getInstance();
        DateFormat usDateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        return usDateFormat.format(calendar.getTime());
    }

//    ----------------------------------------------------------------------------------------------

    public static boolean isNotificationServiceEnabled(Context context) {
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
