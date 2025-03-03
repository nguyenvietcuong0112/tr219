package com.moneytracker.aimoney.moneymanager.finance.service;


import android.content.Context;
import android.os.AsyncTask;

import androidx.recyclerview.widget.RecyclerView;

import com.moneytracker.aimoney.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneytracker.aimoney.moneymanager.finance.adapter.ChatAdapter;
import com.moneytracker.aimoney.moneymanager.finance.model.Message;
import com.moneytracker.aimoney.moneymanager.finance.model.TransactionData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OpenAIApiTask extends AsyncTask<String, Void, TransactionData> {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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";
    private final List<Message> messages;
    private final ChatAdapter adapter;
    private final RecyclerView recyclerView;
    private final Context context;
    private final int loadingMessageIndex;


    public OpenAIApiTask(List<Message> messages, ChatAdapter adapter,
                         RecyclerView recyclerView, Context context,
                         int loadingMessageIndex) {
        this.messages = messages;
        this.adapter = adapter;
        this.recyclerView = recyclerView;
        this.context = context;
        this.loadingMessageIndex = loadingMessageIndex;
    }

    @Override
    protected TransactionData doInBackground(String... params) {
        String inputText = params[0];
        String currency = params[1];

        try {
            // Build prompt
            String prompt = String.format(
                    "Analyze the following financial transaction:\n" +
                            "\"%s\"\n" +
                            "Extract these details:\n" +
                            "1. Category (e.g., Food & Drinks, Transportation)\n" +
                            "2. Description (e.g., breakfast, taxi ride)\n" +
                            "3. Amount (as number)\n" +
                            "4. Type (Income or Expense)\n" +
                            "5. Provide a short, friendly comment about the transaction based on the amount, activity, and type.\n" +
                            "   - If the transaction details are unclear or incomplete, always return: \"This transaction is pending further clarification.\"\n\n" +
                            "Response in JSON format with keys: category, description, amount, type, comment.",
                    inputText
            );

            // Create request body
            JSONObject body = new JSONObject()
                    .put("model", "gpt-4")
                    .put("messages", new JSONArray()
                            .put(new JSONObject()
                                    .put("role", "system")
                                    .put("content", "You are a financial transaction analyzer."))
                            .put(new JSONObject()
                                    .put("role", "user")
                                    .put("content", prompt))
                    );

            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            String content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            JSONObject parsedResponse = new JSONObject(content);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            String currentDate = sdf.format(new Date());

            return new TransactionData(
                    currentDate,
                    parsedResponse.getString("category"),
                    parsedResponse.getString("description"),
                    parsedResponse.getString("amount"),
                    parsedResponse.getString("type"),
                    currency,
                    parsedResponse.getString("comment")

            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(TransactionData result) {
        messages.remove(loadingMessageIndex);
        adapter.notifyItemRemoved(loadingMessageIndex);

        if (result != null) {
            String analyzedMessage = result.getComment();

            if (analyzedMessage == null || analyzedMessage.trim().isEmpty()) {
                analyzedMessage = "I've analyzed your transaction.";
            }
            messages.add(new Message(analyzedMessage, false));
            adapter.notifyItemInserted(messages.size() - 1);

            messages.add(new Message("", false, result));
            adapter.notifyItemInserted(messages.size() - 1);

            SharePreferenceUtils.saveMessages(context, messages);
        } else {
            messages.add(new Message("Sorry, I couldn't analyze that transaction. Please try again.", false));
            adapter.notifyItemInserted(messages.size() - 1);
        }

        recyclerView.scrollToPosition(messages.size() - 1);
    }
}