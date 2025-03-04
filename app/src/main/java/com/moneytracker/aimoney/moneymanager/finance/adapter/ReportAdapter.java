package com.moneytracker.aimoney.moneymanager.finance.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moneytracker.aimoney.moneymanager.finance.R;
import com.moneytracker.aimoney.moneymanager.finance.utils.SharePreferenceUtils;
import com.moneytracker.aimoney.moneymanager.finance.model.ReportData;
import com.moneytracker.aimoney.moneymanager.finance.model.TransactionData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_DATE_OR_TRANSACTION = 1;

    private final ReportData reportData;
    private final List<Object> items = new ArrayList<>();

    public ReportAdapter(ReportData reportData) {
        this.reportData = reportData;
        prepareItems();
    }

    private void prepareItems() {
        items.add(new HeaderItem(reportData.totalIncome, reportData.totalExpenses));

        for (Map.Entry<String, List<TransactionData>> entry : reportData.dailyTransactions.entrySet()) {
            items.add(new DateItem(entry.getKey()));
            items.addAll(entry.getValue());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof HeaderItem ? TYPE_HEADER : TYPE_DATE_OR_TRANSACTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_report_header, parent, false));
        }
        return new CombinedViewHolder(inflater.inflate(R.layout.item_report_unified, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((HeaderItem) item);
            return;
        }

        CombinedViewHolder combinedHolder = (CombinedViewHolder) holder;
        combinedHolder.dateContainer.setVisibility(View.GONE);
        combinedHolder.transactionContainer.setVisibility(View.GONE);

        if (item instanceof DateItem) {
            combinedHolder.bindDate((DateItem) item);
        } else if (item instanceof TransactionData) {
            combinedHolder.bindTransaction((TransactionData) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTotalExpenses;
        private final TextView tvTotalIncome;
        private final TextView tvBalance;
        private final String currency;


        HeaderViewHolder(View view) {
            super(view);
            tvTotalExpenses = view.findViewById(R.id.tv_total_expenses);
            tvTotalIncome = view.findViewById(R.id.tv_total_income);
            tvBalance = view.findViewById(R.id.tv_balance);

            String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(view.getContext());
            if (currentCurrency.isEmpty()) currentCurrency = "USD";
            this.currency = currentCurrency;
        }

        void bind(HeaderItem item) {
            tvTotalExpenses.setText(String.format("%.2f %s", item.totalExpenses, currency));
            tvTotalIncome.setText(String.format("%.2f %s", item.totalIncome, currency));
            tvBalance.setText(String.format("%.2f %s", item.totalIncome - item.totalExpenses, currency));
        }
    }

    static class CombinedViewHolder extends RecyclerView.ViewHolder {
        final View dateContainer;
        final View transactionContainer;
        final TextView tvDate;
        final TextView tvDay;
        final TextView tvCategory;
        final TextView tvDescription;
        final TextView tvAmount;

        CombinedViewHolder(View view) {
            super(view);
            dateContainer = view.findViewById(R.id.date_container);
            transactionContainer = view.findViewById(R.id.transaction_container);
            tvDate = view.findViewById(R.id.tv_date);
            tvDay = view.findViewById(R.id.tv_day);
            tvCategory = view.findViewById(R.id.tv_category);
            tvDescription = view.findViewById(R.id.tv_description);
            tvAmount = view.findViewById(R.id.tv_amount);
        }

        void bindDate(DateItem item) {
            dateContainer.setVisibility(View.VISIBLE);
            try {
                Date date = new SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(item.date);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                tvDate.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
                tvDay.setText(new SimpleDateFormat("EEE", Locale.US).format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        void bindTransaction(TransactionData transaction) {
            transactionContainer.setVisibility(View.VISIBLE);
            tvCategory.setText(transaction.getCategory());
            tvDescription.setText(transaction.getDescription());

            String amountText = transaction.getAmount();
            if (!amountText.startsWith("-") && !amountText.startsWith("+")) {
                amountText = ("Income".equals(transaction.getType()) ? "" : "") + amountText;
            }
            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                amount = 0.0;
            }

            tvAmount.setText(String.format("%.2f %s",
                    amount,
                    transaction.getCurrency()
            ));
            tvAmount.setTextColor(itemView.getContext().getColor(
                    "Income".equals(transaction.getType()) ? R.color.green : R.color.red
            ));
        }
    }

    static class HeaderItem {
        final double totalIncome;
        final double totalExpenses;

        HeaderItem(double totalIncome, double totalExpenses) {
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
        }
    }

    static class DateItem {
        final String date;

        DateItem(String date) {
            this.date = date;
        }
    }
}

