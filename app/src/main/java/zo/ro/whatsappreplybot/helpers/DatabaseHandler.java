package zo.ro.whatsappreplybot.helpers;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseHandler {

    private DatabaseHelper dbHelper;

    public DatabaseHandler(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void handleIncomingMessage(String sender, String message, String reply) {
        @SuppressLint("SimpleDateFormat") String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        dbHelper.insertMessage(sender, message, timestamp, reply);
        dbHelper.deleteOldMessages(); // Clean up old messages
    }
}
