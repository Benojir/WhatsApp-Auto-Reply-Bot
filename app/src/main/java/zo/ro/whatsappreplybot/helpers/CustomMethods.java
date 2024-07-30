package zo.ro.whatsappreplybot.helpers;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CustomMethods {

    public static String getCurrentDateTime(){
        Calendar calendar = Calendar.getInstance();
        DateFormat usDateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        return usDateFormat.format(calendar.getTime());
    }
}
