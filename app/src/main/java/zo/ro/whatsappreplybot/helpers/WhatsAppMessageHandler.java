package zo.ro.whatsappreplybot.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import zo.ro.whatsappreplybot.models.Message;

public class WhatsAppMessageHandler {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executorService;

    public WhatsAppMessageHandler(Context context) {
        dbHelper = new DatabaseHelper(context);
        executorService = Executors.newSingleThreadExecutor();
    }

//    ----------------------------------------------------------------------------------------------

    public void handleIncomingMessage(String sender, String message, String reply) {
        @SuppressLint("SimpleDateFormat") String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        dbHelper.insertMessage(sender, message, timestamp, reply);
        dbHelper.deleteOldMessages(); // Clean up old messages
    }

//    ----------------------------------------------------------------------------------------------

    public void getLast5Messages(String sender, OnMessagesRetrievedListener listener) {
        executorService.execute(() -> {
            List<Message> messages = dbHelper.getLast5MessagesBySender(sender);
            // Use a handler to post the result back to the main thread
            new Handler(Looper.getMainLooper()).post(() -> listener.onMessagesRetrieved(messages));
        });
    }

    public interface OnMessagesRetrievedListener {
        void onMessagesRetrieved(List<Message> messages);
    }
}
