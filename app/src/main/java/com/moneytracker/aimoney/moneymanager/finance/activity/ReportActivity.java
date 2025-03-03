package com.moneytracker.aimoney.moneymanager.finance.activity;


import android.content.Intent;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.moneytracker.aimoney.moneymanager.finance.Utils.AssistiveTouch;
import com.moneytracker.aimoney.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneytracker.aimoney.moneymanager.finance.adapter.ReportAdapter;
import com.moneytracker.aimoney.moneymanager.finance.base.AbsBaseActivity;
import com.moneytracker.aimoney.moneymanager.finance.databinding.ActivityReportBinding;
import com.moneytracker.aimoney.moneymanager.finance.dialog.MonthSelectorDialog;
import com.moneytracker.aimoney.moneymanager.finance.model.Message;
import com.moneytracker.aimoney.moneymanager.finance.model.ReportData;
import com.moneytracker.aimoney.moneymanager.finance.model.TransactionData;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class ReportActivity extends AbsBaseActivity {
    private ActivityReportBinding binding;
    private ReportAdapter adapter;
    private Calendar currentMonth;
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM yyyy", Locale.US);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    @Override
    public void bind() {
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imTransaction.setOnTouchListener(new AssistiveTouch());
        currentMonth = Calendar.getInstance();
        setupViews();
        setupRecyclerView();
        loadTransactions();

        binding.ivBack.setOnClickListener(view -> onBackPressed());

        binding.imTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionActivity.class);
            startActivity(intent);
        });

    }

    private void setupViews() {


        binding.selectDate.setOnClickListener(v -> {
            MonthSelectorDialog dialog = new MonthSelectorDialog(this, calendar -> {
                currentMonth = calendar;
                updateMonthDisplay();
                loadTransactions();
            });
            dialog.show();
        });
        updateMonthDisplay();
    }

    private void updateMonthDisplay() {
        binding.tvCurrentMonth.setText(monthYearFormat.format(currentMonth.getTime()));
    }

    private void setupRecyclerView() {
        binding.rvReport.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadTransactions() {
        List<Message> messages = SharePreferenceUtils.loadMessages(this);
        ReportData reportData = processMessages(messages);
        adapter = new ReportAdapter(reportData);
        binding.rvReport.setAdapter(adapter);
    }

    private ReportData processMessages(List<Message> messages) {
        TreeMap<String, List<TransactionData>> dailyTransactions = new TreeMap<>(Collections.reverseOrder());
        double totalIncome = 0;
        double totalExpenses = 0;

        Calendar start = (Calendar) currentMonth.clone();
        start.set(Calendar.DAY_OF_MONTH, 1);
        Calendar end = (Calendar) currentMonth.clone();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));

        for (Message message : messages) {
            TransactionData transaction = message.getTransactionData();
            if (transaction == null) continue;

            try {
                Date transactionDate = dateFormat.parse(transaction.getDate());
                Calendar transactionCal = Calendar.getInstance();
                transactionCal.setTime(transactionDate);

                if (transactionCal.before(start) || transactionCal.after(end)) {
                    continue;
                }

                dailyTransactions.computeIfAbsent(transaction.getDate(), k -> new ArrayList<>())
                        .add(transaction);

                double amount = Double.parseDouble(transaction.getAmount().replaceAll("[^0-9.]", ""));
                if ("Income".equals(transaction.getType())) {
                    totalIncome += amount;
                } else {
                    totalExpenses += amount;
                }
            } catch (ParseException | NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return new ReportData(dailyTransactions, totalIncome, totalExpenses);
    }
}