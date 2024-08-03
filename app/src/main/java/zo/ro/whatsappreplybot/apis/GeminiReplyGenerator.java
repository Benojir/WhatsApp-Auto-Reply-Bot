package zo.ro.whatsappreplybot.apis;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import zo.ro.whatsappreplybot.R;
import zo.ro.whatsappreplybot.helpers.WhatsAppMessageHandler;
import zo.ro.whatsappreplybot.models.Message;

public class GeminiReplyGenerator {

    private static final String TAG = "MADARA";
    private final String API_KEY;
    private final String LLM_MODEL;
    private final WhatsAppMessageHandler messageHandler;
    private final String defaultReplyMessage;
    private final String aiReplyLanguage;
    private final String botName;

    public GeminiReplyGenerator(Context context, SharedPreferences sharedPreferences, WhatsAppMessageHandler whatsAppMessageHandler) {
        this.messageHandler = whatsAppMessageHandler;
        API_KEY = sharedPreferences.getString("api_key", "not-set").trim();
        LLM_MODEL = sharedPreferences.getString("llm_model", "gemini-1.5-flash");
        defaultReplyMessage = sharedPreferences.getString("default_reply_message", context.getString(R.string.default_bot_message));
        aiReplyLanguage = sharedPreferences.getString("ai_reply_language", "English");
        botName = sharedPreferences.getString("bot_name", "Yuji");
    }

    public void generateReply(String sender, String message, CustomReplyGenerator.OnReplyGeneratedListener listener) {

        messageHandler.getMessagesHistory(sender, messages -> {

            StringBuilder chatHistory = getChatHistory(messages);
            StringBuilder prompt = buildPrompt(sender, message, chatHistory);

            GenerativeModel gm = new GenerativeModel(LLM_MODEL, API_KEY);
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            Content content = new Content.Builder()
                    .addText(prompt.toString())
                    .build();

            Executor executor = Executors.newSingleThreadExecutor();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    listener.onReplyGenerated(resultText);
                    Log.d(TAG, "onSuccess: " + resultText);
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    listener.onReplyGenerated(defaultReplyMessage);
                    Log.e(TAG, "onFailure: ", t);
                }
            }, executor);
        });
    }

//    ----------------------------------------------------------------------------------------------

    private @NonNull StringBuilder buildPrompt(String sender, String message, StringBuilder chatHistory) {

        StringBuilder prompt = new StringBuilder();

        if (!chatHistory.toString().isEmpty()) {
            prompt.append("You are a WhatsApp auto-reply bot named ").append(botName);
            prompt.append(" who pretends to be human and doesn't disclose that you are a bot.");
            prompt.append(" Give appropriate replies according to the latest message and the previous chat history.");
            prompt.append(" Generate only reply texts. Don't add any other texts.");
            prompt.append(" Always reply in ").append(aiReplyLanguage).append(".");
            prompt.append(" Reply within 1 or 2 sentences whenever possible.");
            prompt.append(" Be polite, context-aware, and ensure your replies are relevant to the conversation.");
            prompt.append("\n\n\nMost recent message (from ");
            prompt.append(sender).append("): ");
            prompt.append(message);
            prompt.append("\n\n\nPrevious chat history: \n").append(chatHistory);
            return prompt;
        }

        prompt.append("You are a WhatsApp auto-reply bot named ").append(botName);
        prompt.append("Your task is replying to the incoming message. ");
        prompt.append("Always reply in ").append(aiReplyLanguage);
        prompt.append(". Be polite, context-aware, and ensure your replies are relevant to the conversation.\n\n");
        prompt.append("\n\n\nIncoming message (from ");
        prompt.append(sender).append("): ");
        prompt.append(message);
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
                chatHistory.append("Time: ").append(senderMessageTimestamp);
                chatHistory.append("\n");
                chatHistory.append("My reply: ").append(myReplyToSenderMessage);
                chatHistory.append("\n\n");
            }
        }
        return chatHistory;
    }

//    ----------------------------------------------------------------------------------------------
}
