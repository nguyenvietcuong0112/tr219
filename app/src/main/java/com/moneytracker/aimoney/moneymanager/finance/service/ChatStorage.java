package com.moneytracker.aimoney.moneymanager.finance.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moneytracker.aimoney.moneymanager.finance.model.Message;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatStorage {

    private static final String PREFS_NAME = "chat_prefs";
    private static final String KEY_CHAT_HISTORY = "chat_history";

    private final SharedPreferences sharedPreferences;

    public ChatStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Lưu danh sách tin nhắn
    public void saveChatHistory(List<Message> messages) {
        Gson gson = new Gson();
        String json = gson.toJson(messages);
        sharedPreferences.edit().putString(KEY_CHAT_HISTORY, json).apply();
    }

    // Tải danh sách tin nhắn
    public List<Message> loadChatHistory() {
        String json = sharedPreferences.getString(KEY_CHAT_HISTORY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Message>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }
}