package com.moneytracker.aimoney.moneymanager.finance.adapter;

import android.content.Intent;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.moneytracker.aimoney.moneymanager.finance.R;
import com.moneytracker.aimoney.moneymanager.finance.utils.SharePreferenceUtils;
import com.moneytracker.aimoney.moneymanager.finance.activity.TransactionActivity;
//import com.moneytracker.aimoney.moneymanager.finance.activity.TransactionDetailsActivity;
import com.moneytracker.aimoney.moneymanager.finance.activity.TransactionEditActivity;
import com.moneytracker.aimoney.moneymanager.finance.model.Message;
import com.moneytracker.aimoney.moneymanager.finance.model.TransactionData;

import java.text.NumberFormat;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private static final int VIEW_TYPE_TRANSACTION = 3;

    private final List<Message> messages;

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.isUser()) {
            return VIEW_TYPE_USER;
        } else if (message.getTransactionData() != null) {
            return VIEW_TYPE_TRANSACTION;
        } else {
            return VIEW_TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_TRANSACTION:
                return new TransactionViewHolder(
                        inflater.inflate(R.layout.item_transaction_message, parent, false)
                );
            case VIEW_TYPE_USER:
                return new MessageViewHolder(
                        inflater.inflate(R.layout.item_message_user, parent, false)
                );
            default:
                return new MessageViewHolder(
                        inflater.inflate(R.layout.item_message_bot, parent, false)
                );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof TransactionViewHolder) {
            ((TransactionViewHolder) holder).bind(message);
        } else if (holder instanceof MessageViewHolder) {
            ((MessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void bind(Message message) {
            tvMessage.setText(message.getText());
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvCategory, tvDescription, tvAmount,textEdit,tvType;
        LinearLayout cardTransaction;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvType = itemView.findViewById(R.id.tvType);
            cardTransaction = itemView.findViewById(R.id.cardTransaction);
            textEdit = itemView.findViewById(R.id.textEdit);
        }

        void bind(Message message) {
            TransactionData data = message.getTransactionData();
            if (data == null) return;

            tvDate.setText(data.getDate());
            tvCategory.setText(data.getCategory());
            tvDescription.setText(data.getDescription());
            tvType.setText(data.getType());

            String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(itemView.getContext());
            if (currentCurrency.isEmpty()) currentCurrency = "USD";


            try {
                double amount = Double.parseDouble(data.getAmount());

                NumberFormat numberFormat = NumberFormat.getInstance();
                String amountText = String.format("%s%s %s",
                        data.getType().equals("Income") ? "" : "",
                        numberFormat.format(amount),
                        currentCurrency);

                tvAmount.setText(amountText);

                tvAmount.setTextColor(itemView.getContext().getResources().getColor(
                        data.getType().equals("Income") ? R.color.green : R.color.red,
                        null
                ));
            } catch (NumberFormatException e) {

                tvAmount.setText("Invalid amount");
                tvAmount.setTextColor(itemView.getContext().getResources().getColor(
                        data.getType().equals("Income") ? R.color.green : R.color.red,
                        null
                ));
            }

            String text = "Wrong Input? Edit Transaction";

            SpannableString spannableString = new SpannableString(text);
            spannableString.setSpan(new UnderlineSpan(), 0, text.length(), 0);
            textEdit.setText(spannableString);


            textEdit.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), TransactionEditActivity.class);
                intent.putExtra("transaction_data", data);
                intent.putExtra("transaction_index", getAdapterPosition());

                if (itemView.getContext() instanceof TransactionActivity) {
                    ((TransactionActivity) itemView.getContext())
                            .transactionDetailsLauncher.launch(intent);
                } else {
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }


}