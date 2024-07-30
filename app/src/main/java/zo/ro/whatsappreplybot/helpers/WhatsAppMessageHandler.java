package zo.ro.whatsappreplybot.helpers;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import zo.ro.whatsappreplybot.models.Message;

public class WhatsAppMessageHandler {

    private final DatabaseHelper dbHelper;

    public WhatsAppMessageHandler(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

//    ----------------------------------------------------------------------------------------------

    public void handleIncomingMessage(String sender, String message, String reply) {
        @SuppressLint("SimpleDateFormat") String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        dbHelper.insertMessage(sender, message, timestamp, reply);
        dbHelper.deleteOldMessages(); // Clean up old messages
    }

//    ----------------------------------------------------------------------------------------------

    public void getLast5Messages(String sender, OnMessagesRetrievedListener listener) {
        new Thread(() -> {
            List<Message> messages = dbHelper.getLast5MessagesBySender(sender);
            listener.onMessagesRetrieved(messages);
        }).start();
    }

    public interface OnMessagesRetrievedListener {
        void onMessagesRetrieved(List<Message> messages);
    }
}
