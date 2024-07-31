package zo.ro.whatsappreplybot.apis;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zo.ro.whatsappreplybot.R;
import zo.ro.whatsappreplybot.helpers.WhatsAppMessageHandler;
import zo.ro.whatsappreplybot.models.Message;

public class CustomReplyGenerator {

    private final String API_URL = "https://browser.foganime.com/gpt4free/generate-reply.php";
    private static final String TAG = "MADARA";
    private final String API_KEY;
    private final String LLM_MODEL;
    private final WhatsAppMessageHandler messageHandler;
    private final String defaultReplyMessage;

    public CustomReplyGenerator(Context context, SharedPreferences sharedPreferences, WhatsAppMessageHandler whatsAppMessageHandler) {
        this.messageHandler = whatsAppMessageHandler;
        API_KEY = sharedPreferences.getString("api_key", "").trim();
        LLM_MODEL = sharedPreferences.getString("llm_model", "custom-gpt-4o");
        defaultReplyMessage = sharedPreferences.getString("default_reply_message", context.getString(R.string.default_bot_message));
    }

    public void generateReply(String sender, String message, CustomReplyGenerator.OnReplyGeneratedListener listener) {

        new Thread(() -> messageHandler.getLast5Messages(sender, messages -> {

            StringBuilder chatHistory = getChatHistory(messages);
            StringBuilder prompt = buildPrompt(message, chatHistory);


            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new FormBody.Builder()
                    .add("prompt", prompt.toString())
                    .add("model", LLM_MODEL)
                    .build();

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(requestBody)
                    .build();

            try {
                // Execute the request
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
                            String aiReply = parseResponse(responseData);

                            if (aiReply != null) {
                                listener.onReplyGenerated(aiReply);
                            } else {
                                Log.d(TAG, "onResponse: ai reply is null");
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
        })).start();
    }

//    ----------------------------------------------------------------------------------------------

    private static @NonNull StringBuilder buildPrompt(String message, StringBuilder chatHistory) {

        StringBuilder prompt = new StringBuilder();

        if (chatHistory.toString().isEmpty()) {
            prompt.append("You are a WhatsApp auto-reply bot. Your task is to reply to the incoming message. Response only the chat and do not add any other text.");
            prompt.append("Always respond in Bengali. Be polite, context-aware, and ensure your replies are relevant to the conversation.");
            prompt.append("\n\n\nThis is the most recent message from the sender: ").append(message);
            return prompt;
        }

        prompt.append("You are a WhatsApp auto-reply bot. Your task is to read the provided previous chat history and reply to the most recent incoming message. ");
        prompt.append("Always respond in Bengali. Be polite, context-aware, and ensure your replies are relevant to the conversation.\n\n");
        prompt.append("Previous chat history: \n").append(chatHistory);
        prompt.append("\n\nThis is the most recent message from the sender: ").append(message);
        return prompt;
    }

//    ----------------------------------------------------------------------------------------------

    private @NonNull StringBuilder getChatHistory(List<Message> messages) {

        StringBuilder chatHistory = new StringBuilder();

        if (!messages.isEmpty()) {

            for (Message msg : messages) {

                String senderName = msg.getSender();
                String senderMessage = msg.getMessage();
                String senderMessageTimestamp = msg.getTimestamp();
                String myReplyToSenderMessage = msg.getReply();

                chatHistory.append(senderName).append(": ").append(senderMessage);
                chatHistory.append("\n");
                chatHistory.append("Message sent time: ").append(senderMessageTimestamp);
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
            return jsonObject.getString("response");
        } catch (Exception e) {
            Log.d(TAG, "parseResponse: " + e.getMessage());
        }
        return null;
    }

//    ----------------------------------------------------------------------------------------------

    public interface OnReplyGeneratedListener {
        void onReplyGenerated(String reply);
    }
}
