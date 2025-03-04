package com.moneytracker.aimoney.moneymanager.finance.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.moneytracker.aimoney.moneymanager.finance.utils.SharePreferenceUtils;
import com.moneytracker.aimoney.moneymanager.finance.adapter.ChatAdapter;
import com.moneytracker.aimoney.moneymanager.finance.base.AbsBaseActivity;
import com.moneytracker.aimoney.moneymanager.finance.databinding.TransactionActivityBinding;
import com.moneytracker.aimoney.moneymanager.finance.model.Message;
import com.moneytracker.aimoney.moneymanager.finance.model.TransactionData;
import com.moneytracker.aimoney.moneymanager.finance.service.OpenAIApiTask;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TransactionActivity extends AbsBaseActivity {
    private TransactionActivityBinding binding;
    private BroadcastReceiver currencyChangeReceiver;

    private com.moneytracker.aimoney.moneymanager.finance.adapter.ChatAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    public ActivityResultLauncher<Intent> transactionDetailsLauncher;


    @Override
    public void bind() {
        binding = TransactionActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        final View rootView = findViewById(android.R.id.content);

        binding.ivBack.setOnClickListener(view -> onBackPressed());

        currencyChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("CURRENCY_CHANGED")) {
                    // Refresh all transaction displays
                    adapter.notifyDataSetChanged();
                }
            }
        };

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(currencyChangeReceiver, new IntentFilter("CURRENCY_CHANGED"));
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    binding.llInput.setPadding(0, 0, 0, keypadHeight);
                } else {
                    binding.llInput.setPadding(0, 0, 0, 0);
                }
            }
        });

        transactionDetailsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            TransactionData updatedTransaction =
                                    (TransactionData) data.getSerializableExtra("updated_transaction");

                            if (updatedTransaction != null) {
                                int transactionIndex = data.getIntExtra("transaction_index", -1);

                                if (transactionIndex != -1) {
                                    messages.set(transactionIndex, new Message("", false, updatedTransaction));

                                    adapter.notifyItemChanged(transactionIndex);
                                    SharePreferenceUtils.saveMessages(this, messages);
                                }
                            }
                        }
                    }
                }
        );


        setupTransactionLauncher();
        setupRecyclerView();
        setupSendButton();
        loadSavedMessages();


        if(!messages.isEmpty()) {
            binding.llBot.setVisibility(View.GONE);
        }
//        if (!SharePreferenceUtils.isOrganic(this)) {
//            Admob.getInstance().loadInlineBanner(this, getString(R.string.banner_inline_transaction), Admob.BANNER_INLINE_LARGE_STYLE);
//        } else {
//            binding.adCardView.setVisibility(View.GONE);
//        }

    }

    private void setupTransactionLauncher() {
        transactionDetailsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.getBooleanExtra("delete_transaction", false)) {
                                // Xử lý xóa transaction
                                int transactionIndex = data.getIntExtra("transaction_index", -1);
                                if (transactionIndex != -1) {
                                    deleteTransactionAndRelatedMessages(transactionIndex);
                                }
                            } else {
                                TransactionData updatedTransaction =
                                        (TransactionData) data.getSerializableExtra("updated_transaction");
                                if (updatedTransaction != null) {
                                    int transactionIndex = data.getIntExtra("transaction_index", -1);
                                    if (transactionIndex != -1) {
                                        messages.set(transactionIndex, new Message("", false, updatedTransaction));
                                        adapter.notifyItemChanged(transactionIndex);
                                        SharePreferenceUtils.saveMessages(this, messages);
                                    }
                                }
                            }
                        }
                    }
                }
        );
    }

    private void deleteTransactionAndRelatedMessages(int transactionIndex) {
        int userMessageIndex = -1;
        for (int i = transactionIndex - 1; i >= 0; i--) {
            if (messages.get(i).isUser()) {
                userMessageIndex = i;
                break;
            }
        }

        int botMessageIndex = -1;
        for (int i = transactionIndex - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (!message.isUser() && message.getTransactionData() == null) {
                botMessageIndex = i;
                break;
            }
        }

        List<Integer> indexesToRemove = new ArrayList<>();
        indexesToRemove.add(transactionIndex);
        if (userMessageIndex != -1) indexesToRemove.add(userMessageIndex);
        if (botMessageIndex != -1) indexesToRemove.add(botMessageIndex);

        Collections.sort(indexesToRemove, Collections.reverseOrder());

        for (int index : indexesToRemove) {
            messages.remove(index);
            adapter.notifyItemRemoved(index);
        }

        if (!indexesToRemove.isEmpty()) {
            int minIndex = Collections.min(indexesToRemove);
            adapter.notifyItemRangeChanged(minIndex, messages.size() - minIndex);
        }

        SharePreferenceUtils.saveMessages(this, messages);
    }


    private void setupRecyclerView() {
        adapter = new ChatAdapter(messages);
        binding.rvChat.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChat.setAdapter(adapter);
    }

    private void setupSendButton() {
        binding.btnSend.setOnClickListener(v -> {
            String inputText = binding.etInput.getText().toString().trim();
            if (inputText.isEmpty()) return;

            binding.btnSend.setEnabled(false);

            messages.add(new Message(inputText, true));
            adapter.notifyItemInserted(messages.size() - 1);

            binding.llBot.setVisibility(View.GONE);

            messages.add(new Message("...", false));
            int loadingMessageIndex = messages.size() - 1;
            adapter.notifyItemInserted(loadingMessageIndex);
            binding.rvChat.scrollToPosition(loadingMessageIndex);

            binding.etInput.setText("");

            String currency = SharePreferenceUtils.getSelectedCurrencyCode(this);
            if (currency.isEmpty()) currency = "USD";

            new OpenAIApiTask(messages, adapter, binding.rvChat, this, loadingMessageIndex)
                    .execute(inputText, currency);

            binding.btnSend.setEnabled(true);
        });
    }

    private void loadSavedMessages() {
        List<Message> savedMessages = SharePreferenceUtils.loadMessages(this);
        if (savedMessages != null && !savedMessages.isEmpty()) {
            messages.addAll(savedMessages);
            adapter.notifyDataSetChanged();
            binding.rvChat.scrollToPosition(messages.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currencyChangeReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(currencyChangeReceiver);
        }
    }


}