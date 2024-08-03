package zo.ro.whatsappreplybot.apis;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zo.ro.whatsappreplybot.R;
import zo.ro.whatsappreplybot.helpers.WhatsAppMessageHandler;
import zo.ro.whatsappreplybot.models.Message;

public class ChatGPTReplyGenerator {

    private static final String TAG = "MADARA";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String API_KEY;
    private final String LLM_MODEL;
    private final WhatsAppMessageHandler messageHandler;
    private List<Message> messagesList;
    private final String defaultReplyMessage;
    private final String aiReplyLanguage;

    public ChatGPTReplyGenerator(Context context, SharedPreferences sharedPreferences, WhatsAppMessageHandler whatsAppMessageHandler) {
        this.messageHandler = whatsAppMessageHandler;
        API_KEY = sharedPreferences.getString("api_key", "not-set").trim();
        LLM_MODEL = sharedPreferences.getString("llm_model", "gpt-4o-mini");
        defaultReplyMessage = sharedPreferences.getString("default_reply_message", context.getString(R.string.default_bot_message));
        aiReplyLanguage = sharedPreferences.getString("ai_reply_language", "English");
    }

    public void generateReply(String sender, String message, OnReplyGeneratedListener listener) {

        new Thread(() -> {

            JSONObject container = new JSONObject();
            JSONArray httpRequestMessages = new JSONArray();

            JSONObject systemRole = new JSONObject();
            JSONObject userRole1 = new JSONObject();
            JSONObject userRole2 = new JSONObject();

            messageHandler.getMessagesHistory(sender, messages -> {

                messagesList = messages;

                StringBuilder chatHistory = getChatHistory();

                try {

                    systemRole.put("role", "system");
                    systemRole.put(
                            "content", "You are a WhatsApp auto-reply bot. " +
                                    "Your task is to read the provided previous chat history and reply to the most recent incoming message. " +
                                    "Always respond in " + aiReplyLanguage + ". Be polite, context-aware, and ensure your replies are relevant to the conversation."
                    );

                    userRole1.put("role", "user");

                    if (chatHistory.toString().isEmpty()) {
                        userRole1.put("content", "There are no any previous chat history. This is the first message from the sender.");
                    } else {
                        userRole1.put("content", "Previous chat history: " + chatHistory);
                    }

                    userRole2.put("role", "user");
                    userRole2.put("content", "Most recent message from the sender (" + sender + "): " + message);


                    httpRequestMessages.put(systemRole);
                    httpRequestMessages.put(userRole1);
                    httpRequestMessages.put(userRole2);

                    container.put("model", LLM_MODEL);
                    container.put("messages", httpRequestMessages);
//                container.put("temperature", 0.7);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)  // Set connect timeout
                            .readTimeout(30, TimeUnit.SECONDS)     // Set read timeout
                            .writeTimeout(30, TimeUnit.SECONDS)    // Set write timeout
                            .build();

                    MediaType JSON = MediaType.get("application/json; charset=utf-8");

                    String jsonBody = container.toString();

                    RequestBody requestBody = RequestBody.create(jsonBody, JSON);

                    Request request = new Request.Builder()
                            .url(API_URL)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", "Bearer " + API_KEY)
                            .post(requestBody)
                            .build();

                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e(TAG, "onFailure: ", e);
                            listener.onReplyGenerated(defaultReplyMessage);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                            if (!response.isSuccessful()) {
                                listener.onReplyGenerated(defaultReplyMessage);
                                Log.d(TAG, "onResponse: " + response.code());
                                return;
                            }

                            ResponseBody body = response.body();

                            if (body != null) {
                                String responseData = body.string();
                                String chatGPTReply = parseResponse(responseData);

                                if (chatGPTReply != null) {
                                    listener.onReplyGenerated(chatGPTReply);
                                } else {
                                    Log.d(TAG, "onResponse: chatGPTReply is null");
                                    listener.onReplyGenerated(defaultReplyMessage);
                                    Log.d(TAG, "onResponse: " + responseData);
                                }
                            } else {
                                Log.e(TAG, "onResponse: Response body is null");
                                listener.onReplyGenerated(defaultReplyMessage);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "generateReply: ", e);
                    listener.onReplyGenerated(defaultReplyMessage);
                }
            });
        }).start();
    }

//    ----------------------------------------------------------------------------------------------

    private @NonNull StringBuilder getChatHistory() {
        StringBuilder chatHistory = new StringBuilder();

        Log.d(TAG, "getChatHistory: " + messagesList.size());

        if (messagesList != null && !messagesList.isEmpty()) {

            for (Message msg : messagesList) {

                String senderName = msg.getSender();
                String senderMessage = msg.getMessage();
                String senderMessageTimestamp = msg.getTimestamp();
                String myReplyToSenderMessage = msg.getReply();

                chatHistory.append(senderName).append(": ").append(senderMessage);
                chatHistory.append("\n");
                chatHistory.append("Time: ").append(senderMessageTimestamp);
                chatHistory.append("\n");
                chatHistory.append("My reply: ").append(myReplyToSenderMessage);
                chatHistory.append("\n\n");
            }
        }
        return chatHistory;
    }

//    ----------------------------------------------------------------------------------------------

    private String parseResponse(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray choicesArray = jsonObject.getJSONArray("choices");
            if (choicesArray.length() > 0) {
                JSONObject choice = choicesArray.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                return message.getString("content");
            }
        } catch (Exception e) {
            Log.e(TAG, "parseResponse: ", e);
        }
        return null;
    }

//    ----------------------------------------------------------------------------------------------

    public interface OnReplyGeneratedListener {
        void onReplyGenerated(String reply);
    }
}
