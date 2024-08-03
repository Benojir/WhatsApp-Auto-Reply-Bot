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
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

import zo.ro.whatsappreplybot.R;
import zo.ro.whatsappreplybot.apis.ChatGPTReplyGenerator;
import zo.ro.whatsappreplybot.apis.CustomReplyGenerator;
import zo.ro.whatsappreplybot.apis.GeminiReplyGenerator;
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

                String senderMessage = text.toString();

                if (sharedPreferences.getBoolean("is_bot_enabled", true)) {

                    int maxReply = Integer.parseInt(sharedPreferences.getString("max_reply", "100"));

                    messageHandler.getAllMessagesBySender(title, messages -> {

                        if (messages != null && messages.size() < maxReply) {

                            boolean groupReplyEnabled = sharedPreferences.getBoolean("is_group_reply_enabled", false);

                            if (groupReplyEnabled) {
                                processAutoReply(statusBarNotification, title, senderMessage, messageId);
                            } else {
                                if (!isGroupMessage(title)) {
                                    processAutoReply(statusBarNotification, title, senderMessage, messageId);
                                }
                            }
                        }
                    });
                }
            }

            // Clear the set if it reaches size 50 for ram memory free // but no necessary currently
            if (respondedMessages.size() > 50) {
                respondedMessages.clear();
            }
        }
    }

//    ----------------------------------------------------------------------------------------------

    private void processAutoReply(StatusBarNotification statusBarNotification, String sender, String message, String messageId) {

        Notification.Action[] actions = statusBarNotification.getNotification().actions;

        if (actions != null) {

            for (Notification.Action action : actions) {

                // Here is validating sender's message. Not whatsapp checking for messages
                if (action.getRemoteInputs() != null && action.getRemoteInputs().length > 0) {

                    //..............................................................................

                    String replyPrefix = sharedPreferences.getString("reply_prefix_message", getString(R.string.default_reply_prefix)).trim();

                    if (isAIConfigured()) {

                        String llmModel = sharedPreferences.getString("llm_model", "gpt-4o-mini").toLowerCase();

                        if (llmModel.startsWith("gpt")) {

                            ChatGPTReplyGenerator chatGPTReplyGenerator = new ChatGPTReplyGenerator(this, sharedPreferences, messageHandler);

                            chatGPTReplyGenerator.generateReply(sender, message, reply -> {
                                botReplyMessage = replyPrefix + " " + reply;
                                String botReplyWithoutPrefix = botReplyMessage.replace(replyPrefix, "").trim();
                                messageHandler.handleIncomingMessage(sender, message, botReplyWithoutPrefix);
                                send(action, botReplyMessage);
                                new Handler(Looper.getMainLooper()).postDelayed(() -> respondedMessages.remove(messageId), 750);
                            });

                        } else if (llmModel.startsWith("custom")) {

                            CustomReplyGenerator customReplyGenerator = new CustomReplyGenerator(this, sharedPreferences, messageHandler);

                            customReplyGenerator.generateReply(sender, message, reply -> {
                                botReplyMessage = replyPrefix + " " + reply;
                                String botReplyWithoutPrefix = botReplyMessage.replace(replyPrefix, "").trim();
                                messageHandler.handleIncomingMessage(sender, message, botReplyWithoutPrefix);
                                send(action, botReplyMessage);
                                new Handler(Looper.getMainLooper()).postDelayed(() -> respondedMessages.remove(messageId), 750);
                            });

                        } else if (llmModel.startsWith("gemini")) {

                            GeminiReplyGenerator geminiReplyGenerator = new GeminiReplyGenerator(this, sharedPreferences, messageHandler);

                            geminiReplyGenerator.generateReply(sender, message, reply -> {
                                botReplyMessage = replyPrefix + " " + reply;
                                String botReplyWithoutPrefix = botReplyMessage.replace(replyPrefix, "").trim();
                                messageHandler.handleIncomingMessage(sender, message, botReplyWithoutPrefix);
                                send(action, botReplyMessage);
                                new Handler(Looper.getMainLooper()).postDelayed(() -> respondedMessages.remove(messageId), 750);
                            });
                        }

                    } else {
                        botReplyMessage = (replyPrefix + " " + sharedPreferences.getString("default_reply_message", getString(R.string.default_bot_message))).trim();
                        String botReplyWithoutPrefix = botReplyMessage.replace(replyPrefix, "").trim();
                        messageHandler.handleIncomingMessage(sender, message, botReplyWithoutPrefix);
                        send(action, botReplyMessage);
                        new Handler().postDelayed(() -> respondedMessages.remove(messageId), 750);
                    }

                    //..............................................................................

                    break;
                }
            }
        }
    }

//    ----------------------------------------------------------------------------------------------

    private void send(Notification.Action action, String botReplyMessage) {

        RemoteInput remoteInput = action.getRemoteInputs()[0];

        Intent intent = new Intent();

        Bundle bundle = new Bundle();
        bundle.putCharSequence(remoteInput.getResultKey(), botReplyMessage);

        RemoteInput.addResultsToIntent(new RemoteInput[]{remoteInput}, intent, bundle);

        try {
            action.actionIntent.send(this, 0, intent);
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, "sendAutoReply: ", e);
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
            if (!sharedPreferences.getString("api_key", "").isEmpty()) {
                isAIConfigured = true;
            }
        }
        return isAIConfigured;
    }

//    ----------------------------------------------------------------------------------------------
}