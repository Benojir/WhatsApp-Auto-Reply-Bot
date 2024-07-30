package zo.ro.whatsappreplybot.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

import zo.ro.whatsappreplybot.R;
import zo.ro.whatsappreplybot.helpers.GenerateReplyUsingChatGPT;
import zo.ro.whatsappreplybot.helpers.WhatsAppMessageHandler;

public class MyNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "MADARA";
    private final String notificationChannelId = "wa_auto_reply_channel";
    private final Set<String> respondedMessages = new HashSet<>();
    private SharedPreferences sharedPreferences;
    private WhatsAppMessageHandler messageHandler;
    private String botReplyMessage;

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        super.onNotificationPosted(statusBarNotification);

        if (statusBarNotification.getPackageName().equalsIgnoreCase("com.whatsapp")) {

            Bundle extras = statusBarNotification.getNotification().extras;
            String messageId = statusBarNotification.getKey();
            String title = extras.getString(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

            // Check if we've already responded to this message
            if (respondedMessages.contains(messageId)) {
                return;
            }

            // Add this message to the set of responded messages to avoid looping
            respondedMessages.add(messageId);

            // Process the message and send auto-reply
            if (text != null && !text.toString().isEmpty()) {

                if (sharedPreferences.getBoolean("is_bot_enabled", true)) {

                    boolean groupReplyEnabled = sharedPreferences.getBoolean("is_group_reply_enabled", false);

                    if (groupReplyEnabled){
                        sendAutoReply(statusBarNotification, title, text.toString());
                    } else {
                        if (!isGroupMessage(title)){
                            Log.d(TAG, "onNotificationPosted: it is a group message " + title);
                            sendAutoReply(statusBarNotification, title, text.toString());
                        }
                    }

                    new Handler().postDelayed(() -> respondedMessages.remove(messageId), 750);
                }
            }

            // Clear the set if it reaches size 50 for ram memory free // but no necessary currently
            if (respondedMessages.size() > 50) {
                respondedMessages.clear();
            }
        }
    }

//    ----------------------------------------------------------------------------------------------

    private void sendAutoReply(StatusBarNotification statusBarNotification, String sender, String message) {

        Notification.Action[] actions = statusBarNotification.getNotification().actions;

        if (actions != null) {

            for (Notification.Action action : actions) {

                // Here is validating sender's message. Not whatsapp checking for messages
                if (action.getRemoteInputs() != null && action.getRemoteInputs().length > 0) {

                    RemoteInput remoteInput = action.getRemoteInputs()[0];

                    Intent intent = new Intent();

                    //..............................................................................

                    String replyPrefix = sharedPreferences.getString("reply_prefix_message", getString(R.string.default_reply_prefix));
                    botReplyMessage = replyPrefix + " " + sharedPreferences.getString("default_reply_message", getString(R.string.default_bot_message));

                    if (isAIConfigured()) {
                        GenerateReplyUsingChatGPT generateReplyUsingChatGPT = new GenerateReplyUsingChatGPT(this, sharedPreferences, messageHandler);
                        generateReplyUsingChatGPT.generateReply(sender, message, reply -> botReplyMessage = reply);
                        botReplyMessage = replyPrefix + " " + botReplyMessage;
                    }

                    messageHandler.handleIncomingMessage(sender, message, botReplyMessage.trim());

                    //..............................................................................

                    Bundle bundle = new Bundle();
                    bundle.putCharSequence(remoteInput.getResultKey(), botReplyMessage);

                    RemoteInput.addResultsToIntent(new RemoteInput[]{remoteInput}, intent, bundle);

                    try {
                        Log.d(TAG, "sender's message: " + message);

                        action.actionIntent.send(this, 0, intent);

                    } catch (PendingIntent.CanceledException e) {
                        Log.e(TAG, "sendAutoReply: ", e);
                    }
                    break;
                }
            }
        }
    }

//    ----------------------------------------------------------------------------------------------

    @Override
    public void onCreate() {
        super.onCreate();

        messageHandler = new WhatsAppMessageHandler(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId)
                .setSmallIcon(R.drawable.notifications_24)
                .setContentTitle("Auto-Reply Active")
                .setContentText("WhatsApp auto-reply is running")
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(1, builder.build());
    }

//    ----------------------------------------------------------------------------------------------

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    notificationChannelId,
                    "Auto Reply Service",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Channel for Auto Reply Service");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

//    ----------------------------------------------------------------------------------------------

    private boolean isGroupMessage(String title) {
        return title != null && title.contains(":");
    }

//    ----------------------------------------------------------------------------------------------

    private boolean isAIConfigured() {
        boolean isAIConfigured = false;
        if (sharedPreferences.getBoolean("is_ai_reply_enabled", false)) {
            if (!sharedPreferences.getString("openai_api_key", "").isEmpty()) {
                isAIConfigured = true;
            }
        }
        return isAIConfigured;
    }

//    ----------------------------------------------------------------------------------------------
}