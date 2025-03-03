package com.moneytracker.aimoney.moneymanager.finance.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneytracker.aimoney.moneymanager.finance.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MonthSelectorDialog extends Dialog {
    private final MonthSelectedListener listener;
    private final List<Calendar> months = new ArrayList<>();
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);

    public interface MonthSelectedListener {
        void onMonthSelected(Calendar calendar);
    }

    public MonthSelectorDialog(@NonNull Context context, MonthSelectedListener listener) {
        super(context);
        this.listener = listener;
        setupDialog();
    }

    private void setupDialog() {
        setContentView(R.layout.dialog_month_selector);
        generateMonthsList();

        RecyclerView rvMonths = findViewById(R.id.rvMonths);
        rvMonths.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMonths.setAdapter(new MonthAdapter());
    }

    private void generateMonthsList() {
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            Calendar month = (Calendar) cal.clone();
            months.add(month);
            cal.add(Calendar.MONTH, -1);
        }
    }

    private class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {
        @NonNull
        @Override
        public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_month_selector, parent, false);
            return new MonthViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
            Calendar month = months.get(position);
            holder.bind(month);
        }

        @Override
        public int getItemCount() {
            return months.size();
        }

        class MonthViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvMonth;

            MonthViewHolder(View itemView) {
                super(itemView);
                tvMonth = itemView.findViewById(R.id.tvMonth);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMonthSelected(months.get(position));
                        dismiss();
                    }
                });
            }

            void bind(Calendar month) {
                tvMonth.setText(monthFormat.format(month.getTime()));
            }
        }
    }
}